package io.github.optical002.godot.parser.parser

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.tokenizer.{CharStream, VariantTokenizer}

object VariantParser {

  def parse(content: String)(using Context): ParseResult[Vector[Tag]] = {
    val stream = CharStream(content)
    val tokenizer = VariantTokenizer(stream)

    @scala.annotation.tailrec
    def collectTokens(acc: Vector[Token]): ParseResult[Vector[Token]] =
      tokenizer.getToken() match {
        case Left(err) => Left(err)
        case Right(token) =>
          if (token.tokenType == TokenType.EOF) {
            Right(acc)
          } else {
            collectTokens(acc :+ token)
          }
      }

    collectTokens(Vector.empty).flatMap { tokens =>
      parseTags(new TokenIterator(tokens))
    }
  }

  def parseTags(tokens: TokenIterator): ParseResult[Vector[Tag]] = {
    @scala.annotation.tailrec
    def collectTags(acc: Vector[Tag]): ParseResult[Vector[Tag]] =
      if (!tokens.hasNext) {
        Right(acc)
      } else {
        parseTagWithProperties(tokens) match {
          case Left(err) => Left(err)
          case Right(tag) => collectTags(acc :+ tag)
        }
      }

    collectTags(Vector.empty)
  }

  def parseTagWithProperties(tokens: TokenIterator): ParseResult[Tag] =
    parseTag(tokens).flatMap { tag =>
      @scala.annotation.tailrec
      def parseProperties(acc: Map[String, Variant]): ParseResult[Tag] =
        if (!tokens.hasNext || tokens.peek().tokenType == TokenType.BracketOpen) {
          Right(tag.copy(fields = tag.fields ++ acc))
        } else {
          parsePropertyAssignment(tokens) match {
            case Left(err) => Left(err)
            case Right((key, value)) => parseProperties(acc + (key -> value))
          }
        }

      parseProperties(Map.empty)
    }

  def parseTag(tokens: TokenIterator): ParseResult[Tag] = {
    val startLine = tokens.currentLine

    if (!tokens.hasNext || tokens.next().tokenType != TokenType.BracketOpen) {
      Left(ParseError.SyntaxError(
        "Expected '[' to start tag",
        startLine,
        "",
        Some("["),
        None
      ))
    } else if (!tokens.hasNext) {
      Left(ParseError.SyntaxError(
        "Expected tag name after '['",
        startLine,
        "",
        Some("identifier"),
        Some("EOF")
      ))
    } else {
      val nameToken = tokens.next()
      nameToken.value.asString.orElse(nameToken.value.asStringName).toRight(
        ParseError.SyntaxError(
          s"Expected identifier for tag name, got ${nameToken.tokenType}",
          nameToken.line,
          "",
          Some("identifier"),
          Some(nameToken.tokenType.toString)
        )
      ).flatMap { tagName =>
        @scala.annotation.tailrec
        def parseFields(acc: Map[String, Variant]): ParseResult[Tag] =
          if (!tokens.hasNext) {
            Left(ParseError.SyntaxError(
              "Unexpected EOF in tag",
              tokens.currentLine,
              "",
              Some("]"),
              Some("EOF")
            ))
          } else if (tokens.peek().tokenType == TokenType.BracketClose) {
            tokens.next() // consume closing bracket
            Right(Tag(tagName, acc, startLine))
          } else {
            val keyToken = tokens.next()
            keyToken.value.asString.orElse(keyToken.value.asStringName) match {
              case None =>
                Left(ParseError.SyntaxError(
                  s"Expected identifier for field key, got ${keyToken.tokenType}",
                  keyToken.line,
                  "",
                  Some("identifier"),
                  Some(keyToken.tokenType.toString)
                ))
              case Some(key) =>
                if (!tokens.hasNext || tokens.next().tokenType != TokenType.Equal) {
                  Left(ParseError.SyntaxError(
                    "Expected '=' after field key",
                    tokens.currentLine,
                    "",
                    Some("="),
                    None
                  ))
                } else {
                  parseValue(tokens) match {
                    case Left(err) => Left(err)
                    case Right(value) => parseFields(acc + (key -> value))
                  }
                }
            }
          }

        parseFields(Map.empty)
      }
    }
  }

  def parsePropertyAssignment(tokens: TokenIterator): ParseResult[(String, Variant)] =
    if (!tokens.hasNext) {
      Left(ParseError.SyntaxError(
        "Expected property key",
        0,
        "",
        Some("identifier"),
        Some("EOF")
      ))
    } else {
      val keyToken = tokens.next()
      for {
        key <- keyToken.value.asString.orElse(keyToken.value.asStringName).toRight(
          ParseError.SyntaxError(
            s"Expected identifier for property key, got ${keyToken.tokenType}",
            keyToken.line,
            "",
            Some("identifier"),
            Some(keyToken.tokenType.toString)
          )
        )
        _ <- if (tokens.hasNext && tokens.next().tokenType == TokenType.Equal) {
          Right(())
        } else {
          Left(ParseError.SyntaxError(
            "Expected '=' after property key",
            tokens.currentLine,
            "",
            Some("="),
            None
          ))
        }
        value <- parseValue(tokens)
      } yield (key, value)
    }

  def parseValue(tokens: TokenIterator): ParseResult[Variant] =
    if (!tokens.hasNext) {
      Left(ParseError.SyntaxError(
        "Expected value",
        0,
        "",
        Some("value"),
        Some("EOF")
      ))
    } else {
      val token = tokens.peek()

      token.tokenType match {
      // Direct values from tokenizer
      case TokenType.String | TokenType.StringName | TokenType.Number | TokenType.Color =>
        Right(tokens.next().value)

      // Identifiers - could be keywords (true/false/null/nil) or constructs
      case TokenType.Identifier =>
        val idToken = tokens.next()
        idToken.value.asString.orElse(idToken.value.asStringName) match {
          case Some("true") => Right(Variant.Bool(true))
          case Some("false") => Right(Variant.Bool(false))
          case Some("null") => Right(Variant.Object(ObjectValue.Null))
          case Some("nil") => Right(Variant.Nil)
          case Some("inf") => Right(Variant.Float(Double.PositiveInfinity))
          case Some("inf_neg") => Right(Variant.Float(Double.NegativeInfinity))
          case Some("nan") => Right(Variant.Float(Double.NaN))
          case Some(name) =>
            // Check for ObjectValue.ConstructName(...) pattern
            if (name == "ObjectValue" && tokens.hasNext && tokens.peek().tokenType == TokenType.Period) {
              tokens.next() // Consume the period
              if (tokens.hasNext && tokens.peek().tokenType == TokenType.Identifier) {
                val constructToken = tokens.next()
                constructToken.value.asString.orElse(constructToken.value.asStringName) match {
                  case Some("Null") =>
                    // ObjectValue.Null is a special case for null object references
                    Right(Variant.Object(ObjectValue.Null))
                  case Some(constructName) =>
                    if (tokens.hasNext && tokens.peek().tokenType == TokenType.ParenthesisOpen) {
                      ConstructParser.parseConstruct(constructName, tokens)
                    } else {
                      Left(ParseError.SyntaxError(
                        s"Expected '(' after ObjectValue.$constructName",
                        tokens.currentLine,
                        "",
                        Some("'('"),
                        Some(if (tokens.hasNext) tokens.peek().tokenType.toString else "end of input")
                      ))
                    }
                  case None =>
                    Left(ParseError.SyntaxError(
                      "Invalid construct name after ObjectValue.",
                      tokens.currentLine,
                      "",
                      Some("construct name"),
                      None
                    ))
                }
              } else {
                Left(ParseError.SyntaxError(
                  "Expected construct name after ObjectValue.",
                  tokens.currentLine,
                  "",
                  Some("identifier"),
                  Some(if (tokens.hasNext) tokens.peek().tokenType.toString else "end of input")
                ))
              }
            } else if (tokens.hasNext && tokens.peek().tokenType == TokenType.ParenthesisOpen) {
              // Check if followed by parenthesis (construct)
              ConstructParser.parseConstruct(name, tokens)
            } else {
              // Just an identifier value - treat as string
              Right(Variant.String(name))
            }
          case None =>
            Left(ParseError.SyntaxError(
              "Invalid identifier value",
              idToken.line,
              "",
              Some("valid identifier"),
              None
            ))
        }

      // Array
      case TokenType.BracketOpen =>
        parseArray(tokens)

      // Dictionary
      case TokenType.CurlyBracketOpen =>
        parseDictionary(tokens)

      case other =>
        Left(ParseError.SyntaxError(
          s"Unexpected token type for value: $other",
          token.line,
          "",
          Some("value"),
          Some(other.toString)
        ))
      }
    }

  def parseArray(tokens: TokenIterator): ParseResult[Variant] = {
    val startLine = tokens.currentLine
    tokens.next() // Consume opening bracket

    @scala.annotation.tailrec
    def parseElements(acc: Vector[Variant]): ParseResult[Variant] =
      if (!tokens.hasNext) {
        Left(ParseError.SyntaxError(
          "Unexpected EOF in array",
          startLine,
          "",
          Some("]"),
          Some("EOF")
        ))
      } else if (tokens.peek().tokenType == TokenType.BracketClose) {
        tokens.next() // consume closing bracket
        Right(Variant.Array(acc, None))
      } else {
        parseValue(tokens) match {
          case Left(err) => Left(err)
          case Right(value) =>
            if (!tokens.hasNext) {
              Left(ParseError.SyntaxError(
                "Unexpected EOF after array element",
                startLine,
                "",
                Some("',' or ']'"),
                Some("EOF")
              ))
            } else {
              tokens.peek().tokenType match {
                case TokenType.Comma =>
                  tokens.next() // consume comma
                  parseElements(acc :+ value)
                case TokenType.BracketClose =>
                  parseElements(acc :+ value)
                case other =>
                  Left(ParseError.SyntaxError(
                    "Expected ',' or ']' after array element",
                    tokens.peek().line,
                    "",
                    Some("',' or ']'"),
                    Some(other.toString)
                  ))
              }
            }
        }
      }

    parseElements(Vector.empty)
  }

  def parseDictionary(tokens: TokenIterator): ParseResult[Variant] = {
    val startLine = tokens.currentLine
    tokens.next() // Consume opening brace

    @scala.annotation.tailrec
    def parseEntries(acc: Map[String, Variant]): ParseResult[Variant] =
      if (!tokens.hasNext) {
        Left(ParseError.SyntaxError(
          "Unexpected EOF in dictionary",
          startLine,
          "",
          Some("}"),
          Some("EOF")
        ))
      } else if (tokens.peek().tokenType == TokenType.CurlyBracketClose) {
        tokens.next() // consume closing brace
        Right(Variant.Dictionary(acc, None))
      } else {
        parseValue(tokens) match {
          case Left(err) => Left(err)
          case Right(keyVariant) =>
            val key = keyVariant.asString.orElse(keyVariant.asStringName).getOrElse(keyVariant.asKey.toString)

            if (!tokens.hasNext || tokens.next().tokenType != TokenType.Colon) {
              Left(ParseError.SyntaxError(
                "Expected ':' after dictionary key",
                tokens.currentLine,
                "",
                Some(":"),
                None
              ))
            } else {
              parseValue(tokens) match {
                case Left(err) => Left(err)
                case Right(value) =>
                  if (!tokens.hasNext) {
                    Left(ParseError.SyntaxError(
                      "Unexpected EOF after dictionary value",
                      startLine,
                      "",
                      Some("',' or '}'"),
                      Some("EOF")
                    ))
                  } else {
                    tokens.peek().tokenType match {
                      case TokenType.Comma =>
                        tokens.next() // consume comma
                        parseEntries(acc + (key -> value))
                      case TokenType.CurlyBracketClose =>
                        parseEntries(acc + (key -> value))
                      case other =>
                        Left(ParseError.SyntaxError(
                          "Expected ',' or '}' after dictionary entry",
                          tokens.peek().line,
                          "",
                          Some("',' or '}'"),
                          Some(other.toString)
                        ))
                    }
                  }
              }
            }
        }
      }

    parseEntries(Map.empty)
  }
}
