package io.github.optical002.godot.parser.parser

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.tokenizer.{CharStream, VariantTokenizer}

object VariantParser {

  def parse(content: String): ParseResult[Vector[Tag]] = {
    val stream = CharStream(content)
    val tokenizer = VariantTokenizer(stream)

    // Collect all tokens
    val tokensBuilder = Vector.newBuilder[Token]
    var continue = true
    while (continue) {
      tokenizer.getToken() match {
        case Right(token) =>
          if (token.tokenType == TokenType.EOF) {
            continue = false
          } else {
            tokensBuilder += token
          }
        case Left(err) => return Left(err)
      }
    }

    val tokens = new TokenIterator(tokensBuilder.result())
    parseTags(tokens)
  }

  def parseTags(tokens: TokenIterator): ParseResult[Vector[Tag]] = {
    val tags = Vector.newBuilder[Tag]

    while (tokens.hasNext) {
      parseTagWithProperties(tokens) match {
        case Right(tag) => tags += tag
        case Left(err) => return Left(err)
      }
    }

    Right(tags.result())
  }

  def parseTagWithProperties(tokens: TokenIterator): ParseResult[Tag] = {
    // Parse the tag itself
    parseTag(tokens) match {
      case Right(tag) =>
        // Parse following property assignments
        val properties = scala.collection.mutable.Map[String, Variant]()

        while (tokens.hasNext) {
          val token = tokens.peek()

          // Check if next token is a tag
          if (token.tokenType == TokenType.BracketOpen) {
            // Next tag, return current tag with properties
            return Right(tag.copy(fields = tag.fields ++ properties.toMap))
          }

          // Parse property assignment
          parsePropertyAssignment(tokens) match {
            case Right((key, value)) =>
              properties(key) = value
            case Left(err) => return Left(err)
          }
        }

        // End of file
        Right(tag.copy(fields = tag.fields ++ properties.toMap))

      case Left(err) => Left(err)
    }
  }

  def parseTag(tokens: TokenIterator): ParseResult[Tag] = {
    val startLine = tokens.currentLine

    // Expect opening bracket
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.BracketOpen) {
      return Left(ParseError.SyntaxError(
        "Expected '[' to start tag",
        startLine,
        "",
        Some("["),
        None
      ))
    }

    // Parse tag name
    if (!tokens.hasNext) {
      return Left(ParseError.SyntaxError(
        "Expected tag name after '['",
        startLine,
        "",
        Some("identifier"),
        Some("EOF")
      ))
    }

    val nameToken = tokens.next()
    val tagName = nameToken.value.asString.orElse(nameToken.value.asStringName).getOrElse {
      return Left(ParseError.SyntaxError(
        s"Expected identifier for tag name, got ${nameToken.tokenType}",
        nameToken.line,
        "",
        Some("identifier"),
        Some(nameToken.tokenType.toString)
      ))
    }

    // Parse tag fields (key=value pairs)
    val fields = scala.collection.mutable.Map[String, Variant]()

    while (tokens.hasNext) {
      val token = tokens.peek()

      if (token.tokenType == TokenType.BracketClose) {
        tokens.next() // consume closing bracket
        return Right(Tag(tagName, fields.toMap, startLine))
      }

      // Parse field key
      val keyToken = tokens.next()
      val key = keyToken.value.asString.orElse(keyToken.value.asStringName).getOrElse {
        return Left(ParseError.SyntaxError(
          s"Expected identifier for field key, got ${keyToken.tokenType}",
          keyToken.line,
          "",
          Some("identifier"),
          Some(keyToken.tokenType.toString)
        ))
      }

      // Expect equals
      if (!tokens.hasNext || tokens.next().tokenType != TokenType.Equal) {
        return Left(ParseError.SyntaxError(
          "Expected '=' after field key",
          tokens.currentLine,
          "",
          Some("="),
          None
        ))
      }

      // Parse field value
      parseValue(tokens) match {
        case Right(value) =>
          fields(key) = value
        case Left(err) => return Left(err)
      }
    }

    Left(ParseError.SyntaxError(
      "Unexpected EOF in tag",
      tokens.currentLine,
      "",
      Some("]"),
      Some("EOF")
    ))
  }

  def parsePropertyAssignment(tokens: TokenIterator): ParseResult[(String, Variant)] = {
    if (!tokens.hasNext) {
      return Left(ParseError.SyntaxError(
        "Expected property key",
        0,
        "",
        Some("identifier"),
        Some("EOF")
      ))
    }

    // Parse property key
    val keyToken = tokens.next()
    val key = keyToken.value.asString.orElse(keyToken.value.asStringName).getOrElse {
      return Left(ParseError.SyntaxError(
        s"Expected identifier for property key, got ${keyToken.tokenType}",
        keyToken.line,
        "",
        Some("identifier"),
        Some(keyToken.tokenType.toString)
      ))
    }

    // Expect equals
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.Equal) {
      return Left(ParseError.SyntaxError(
        "Expected '=' after property key",
        tokens.currentLine,
        "",
        Some("="),
        None
      ))
    }

    // Parse property value
    parseValue(tokens) match {
      case Right(value) => Right((key, value))
      case Left(err) => Left(err)
    }
  }

  def parseValue(tokens: TokenIterator): ParseResult[Variant] = {
    if (!tokens.hasNext) {
      return Left(ParseError.SyntaxError(
        "Expected value",
        0,
        "",
        Some("value"),
        Some("EOF")
      ))
    }

    val token = tokens.peek()

    token.tokenType match {
      // Direct values from tokenizer
      case TokenType.String | TokenType.StringName | TokenType.Number | TokenType.Color =>
        Right(tokens.next().value)

      // Identifiers - could be keywords (true/false/null) or constructs
      case TokenType.Identifier =>
        val idToken = tokens.next()
        idToken.value.asString.orElse(idToken.value.asStringName) match {
          case Some("true") => Right(Variant.Bool(true))
          case Some("false") => Right(Variant.Bool(false))
          case Some("null") => Right(Variant.Nil)
          case Some(name) =>
            // Check if followed by parenthesis (construct)
            if (tokens.hasNext && tokens.peek().tokenType == TokenType.ParenthesisOpen) {
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

    // Consume opening bracket
    tokens.next()

    val elements = Vector.newBuilder[Variant]

    while (tokens.hasNext) {
      val token = tokens.peek()

      if (token.tokenType == TokenType.BracketClose) {
        tokens.next() // consume closing bracket
        return Right(Variant.Array(elements.result(), None))
      }

      // Parse element
      parseValue(tokens) match {
        case Right(value) =>
          elements += value

          // Check for comma or closing bracket
          if (tokens.hasNext) {
            val next = tokens.peek()
            if (next.tokenType == TokenType.Comma) {
              tokens.next() // consume comma
            } else if (next.tokenType != TokenType.BracketClose) {
              return Left(ParseError.SyntaxError(
                "Expected ',' or ']' after array element",
                next.line,
                "",
                Some("',' or ']'"),
                Some(next.tokenType.toString)
              ))
            }
          }

        case Left(err) => return Left(err)
      }
    }

    Left(ParseError.SyntaxError(
      "Unexpected EOF in array",
      startLine,
      "",
      Some("]"),
      Some("EOF")
    ))
  }

  def parseDictionary(tokens: TokenIterator): ParseResult[Variant] = {
    val startLine = tokens.currentLine

    // Consume opening brace
    tokens.next()

    val entries = scala.collection.mutable.Map[String, Variant]()

    while (tokens.hasNext) {
      val token = tokens.peek()

      if (token.tokenType == TokenType.CurlyBracketClose) {
        tokens.next() // consume closing brace
        return Right(Variant.Dictionary(entries.toMap, None))
      }

      // Parse key
      parseValue(tokens) match {
        case Right(keyVariant) =>
          val key = keyVariant.asString.orElse(keyVariant.asStringName).getOrElse(keyVariant.asKey.toString)

          // Expect colon
          if (!tokens.hasNext || tokens.next().tokenType != TokenType.Colon) {
            return Left(ParseError.SyntaxError(
              "Expected ':' after dictionary key",
              tokens.currentLine,
              "",
              Some(":"),
              None
            ))
          }

          // Parse value
          parseValue(tokens) match {
            case Right(value) =>
              entries(key) = value

              // Check for comma or closing brace
              if (tokens.hasNext) {
                val next = tokens.peek()
                if (next.tokenType == TokenType.Comma) {
                  tokens.next() // consume comma
                } else if (next.tokenType != TokenType.CurlyBracketClose) {
                  return Left(ParseError.SyntaxError(
                    "Expected ',' or '}' after dictionary entry",
                    next.line,
                    "",
                    Some("',' or '}'"),
                    Some(next.tokenType.toString)
                  ))
                }
              }

            case Left(err) => return Left(err)
          }

        case Left(err) => return Left(err)
      }
    }

    Left(ParseError.SyntaxError(
      "Unexpected EOF in dictionary",
      startLine,
      "",
      Some("}"),
      Some("EOF")
    ))
  }
}
