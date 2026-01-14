package io.github.optical002.godot.parser.tokenizer

import io.github.optical002.godot.parser.core.*

class VariantTokenizer(stream: CharStream) {

  //noinspection AccessorLikeMethodIsEmptyParen
  def getToken(): ParseResult[Token] = {
    skipWhitespaceAndComments()

    if (stream.isEof) {
      return Right(Token(TokenType.EOF, Variant.Nil, stream.currentLine, stream.currentColumn))
    }

    val startLine = stream.currentLine
    val startColumn = stream.currentColumn
    val cchar = stream.getChar()

    val result = cchar match {
      case '{' => Right((TokenType.CurlyBracketOpen, Variant.Nil))
      case '}' => Right((TokenType.CurlyBracketClose, Variant.Nil))
      case '[' => Right((TokenType.BracketOpen, Variant.Nil))
      case ']' => Right((TokenType.BracketClose, Variant.Nil))
      case '(' => Right((TokenType.ParenthesisOpen, Variant.Nil))
      case ')' => Right((TokenType.ParenthesisClose, Variant.Nil))
      case ':' => Right((TokenType.Colon, Variant.Nil))
      case ',' => Right((TokenType.Comma, Variant.Nil))
      case '.' => Right((TokenType.Period, Variant.Nil))
      case '=' => Right((TokenType.Equal, Variant.Nil))
      case '#' => parseColor()
      case '&' | '@' => parseStringName() // @ for 3.x compatibility
      case '"' => parseString(isStringName = false)
      case '-' => parseNumber(hasSign = true)
      case c if c.isDigit => stream.saveChar(c); parseNumber(hasSign = false)
      case c if c.isLetter || c == '_' => stream.saveChar(c); parseIdentifier()
      case c => Left(ParseError.TokenizeError(
        s"Unexpected character: '$c' (ASCII ${c.toInt})",
        startLine,
        startColumn,
        stream.getContext()
      ))
    }

    result.map { case (tokenType, value) =>
      Token(tokenType, value, startLine, startColumn)
    }
  }

  private def skipWhitespaceAndComments(): Unit = {
    var continue = true
    while (continue && !stream.isEof) {
      val c = stream.getChar()
      if (c == ';') {
        // Comment - skip to end of line
        while (!stream.isEof && stream.getChar() != '\n') {}
      } else if (c > 32) {
        // Non-whitespace, save and stop
        stream.saveChar(c)
        continue = false
      }
      // Otherwise continue (whitespace)
    }
  }

  private def parseColor(): ParseResult[(TokenType, Variant)] = {
    val colorStr = new StringBuilder("#")

    while (!stream.isEof) {
      val c = stream.getChar()
      if (isHexDigit(c)) {
        colorStr.append(c)
      } else {
        stream.saveChar(c)
        return parseColorFromHex(colorStr.toString).map(color =>
          (TokenType.Color, color)
        )
      }
    }

    parseColorFromHex(colorStr.toString).map(color => (TokenType.Color, color))
  }

  private def parseColorFromHex(hex: String): ParseResult[Variant] = {
    // Parse #RGB, #RGBA, #RRGGBB, #RRGGBBAA
    val digits = hex.drop(1) // Remove #

    val (r, g, b, a) = digits.length match {
      case 3 => // #RGB
        val r = Integer.parseInt(digits.substring(0, 1) * 2, 16) / 255.0
        val g = Integer.parseInt(digits.substring(1, 2) * 2, 16) / 255.0
        val b = Integer.parseInt(digits.substring(2, 3) * 2, 16) / 255.0
        (r, g, b, 1.0)

      case 4 => // #RGBA
        val r = Integer.parseInt(digits.substring(0, 1) * 2, 16) / 255.0
        val g = Integer.parseInt(digits.substring(1, 2) * 2, 16) / 255.0
        val b = Integer.parseInt(digits.substring(2, 3) * 2, 16) / 255.0
        val a = Integer.parseInt(digits.substring(3, 4) * 2, 16) / 255.0
        (r, g, b, a)

      case 6 => // #RRGGBB
        val r = Integer.parseInt(digits.substring(0, 2), 16) / 255.0
        val g = Integer.parseInt(digits.substring(2, 4), 16) / 255.0
        val b = Integer.parseInt(digits.substring(4, 6), 16) / 255.0
        (r, g, b, 1.0)

      case 8 => // #RRGGBBAA
        val r = Integer.parseInt(digits.substring(0, 2), 16) / 255.0
        val g = Integer.parseInt(digits.substring(2, 4), 16) / 255.0
        val b = Integer.parseInt(digits.substring(4, 6), 16) / 255.0
        val a = Integer.parseInt(digits.substring(6, 8), 16) / 255.0
        (r, g, b, a)

      case _ =>
        return Left(ParseError.TokenizeError(
          s"Invalid color format: $hex",
          stream.currentLine,
          stream.currentColumn,
          stream.getContext()
        ))
    }

    Right(Variant.Color(r, g, b, a))
  }

  private def parseStringName(): ParseResult[(TokenType, Variant)] = {
    val nextChar = stream.getChar()
    if (nextChar != '"') {
      return Left(ParseError.TokenizeError(
        s"Expected '\"' after '&', got '$nextChar'",
        stream.currentLine,
        stream.currentColumn,
        stream.getContext()
      ))
    }

    parseString(isStringName = true)
  }

  private def parseString(isStringName: Boolean): ParseResult[(TokenType, Variant)] = {
    val str = new StringBuilder()
    var prevSurrogate: Option[Int] = None

    while (!stream.isEof) {
      val c = stream.getChar()

      if (c == 0) {
        return Left(ParseError.TokenizeError(
          "Unterminated string (unexpected EOF)",
          stream.currentLine,
          stream.currentColumn,
          stream.getContext()
        ))
      } else if (c == '"') {
        // End of string
        if (prevSurrogate.isDefined) {
          return Left(ParseError.TokenizeError(
            "Invalid UTF-16 sequence: unpaired lead surrogate",
            stream.currentLine,
            stream.currentColumn,
            stream.getContext()
          ))
        }

        val value = str.toString
        return Right(
          if (isStringName) (TokenType.StringName, Variant.StringName(value))
          else (TokenType.String, Variant.String(value))
        )

      } else if (c == '\\') {
        // Escape sequence
        val nextChar = stream.getChar()
        if (nextChar == 0) {
          return Left(ParseError.TokenizeError(
            "Unterminated string (EOF in escape)",
            stream.currentLine,
            stream.currentColumn,
            stream.getContext()
          ))
        }

        val escaped = nextChar match {
          case 'b' => Right('\b'.toInt)
          case 't' => Right('\t'.toInt)
          case 'n' => Right('\n'.toInt)
          case 'f' => Right('\f'.toInt)
          case 'r' => Right('\r'.toInt)
          case 'u' => parseUnicodeEscape(4)
          case 'U' => parseUnicodeEscape(6)
          case other => Right(other.toInt)
        }

        escaped match {
          case Left(err) => return Left(err)
          case Right(codePoint) =>
            // Handle UTF-16 surrogates
            if ((codePoint & 0xfffffc00) == 0xd800) {
              // Lead surrogate
              if (prevSurrogate.isDefined) {
                return Left(ParseError.TokenizeError(
                  "Invalid UTF-16 sequence: unpaired lead surrogate",
                  stream.currentLine,
                  stream.currentColumn,
                  stream.getContext()
                ))
              }
              prevSurrogate = Some(codePoint)
            } else if ((codePoint & 0xfffffc00) == 0xdc00) {
              // Trail surrogate
              prevSurrogate match {
                case Some(lead) =>
                  val combined = (lead << 10) + codePoint - ((0xd800 << 10) + 0xdc00 - 0x10000)
                  str.append(combined.toChar)
                  prevSurrogate = None
                case None =>
                  return Left(ParseError.TokenizeError(
                    "Invalid UTF-16 sequence: unpaired trail surrogate",
                    stream.currentLine,
                    stream.currentColumn,
                    stream.getContext()
                  ))
              }
            } else {
              // Regular character
              if (prevSurrogate.isDefined) {
                return Left(ParseError.TokenizeError(
                  "Invalid UTF-16 sequence: unpaired lead surrogate",
                  stream.currentLine,
                  stream.currentColumn,
                  stream.getContext()
                ))
              }
              str.append(codePoint.toChar)
            }
        }

      } else {
        // Regular character
        if (prevSurrogate.isDefined) {
          return Left(ParseError.TokenizeError(
            "Invalid UTF-16 sequence: unpaired lead surrogate",
            stream.currentLine,
            stream.currentColumn,
            stream.getContext()
          ))
        }
        str.append(c)
      }
    }

    Left(ParseError.TokenizeError(
      "Unterminated string",
      stream.currentLine,
      stream.currentColumn,
      stream.getContext()
    ))
  }

  private def parseUnicodeEscape(length: Int): ParseResult[Int] = {
    var codePoint = 0

    for (_ <- 0 until length) {
      val c = stream.getChar()
      if (!isHexDigit(c)) {
        return Left(ParseError.TokenizeError(
          s"Invalid hex digit in unicode escape: '$c'",
          stream.currentLine,
          stream.currentColumn,
          stream.getContext()
        ))
      }

      val digit = if (c.isDigit) c - '0'
                  else if (c >= 'a' && c <= 'f') c - 'a' + 10
                  else if (c >= 'A' && c <= 'F') c - 'A' + 10
                  else 0

      codePoint = (codePoint << 4) | digit
    }

    Right(codePoint)
  }

  private def parseNumber(hasSign: Boolean): ParseResult[(TokenType, Variant)] = {
    val numStr = new StringBuilder()
    if (hasSign) numStr.append('-')

    var isFloat = false
    var state = if (hasSign) NumberState.Integer else NumberState.Start

    while (!stream.isEof && state != NumberState.Done) {
      val c = stream.getChar()

      state match {
        case NumberState.Start | NumberState.Integer =>
          if (c.isDigit) {
            numStr.append(c)
            state = NumberState.Integer
          } else if (c == '.') {
            numStr.append(c)
            isFloat = true
            state = NumberState.Decimal
          } else if (c == 'e' || c == 'E') {
            numStr.append(c)
            isFloat = true
            state = NumberState.ExpSign
          } else {
            stream.saveChar(c)
            state = NumberState.Done
          }

        case NumberState.Decimal =>
          if (c.isDigit) {
            numStr.append(c)
          } else if (c == 'e' || c == 'E') {
            numStr.append(c)
            state = NumberState.ExpSign
          } else {
            stream.saveChar(c)
            state = NumberState.Done
          }

        case NumberState.ExpSign =>
          if (c == '+' || c == '-') {
            numStr.append(c)
            state = NumberState.Exponent
          } else if (c.isDigit) {
            numStr.append(c)
            state = NumberState.Exponent
          } else {
            return Left(ParseError.TokenizeError(
              s"Invalid exponent in number",
              stream.currentLine,
              stream.currentColumn,
              stream.getContext()
            ))
          }

        case NumberState.Exponent =>
          if (c.isDigit) {
            numStr.append(c)
          } else {
            stream.saveChar(c)
            state = NumberState.Done
          }

        case NumberState.Done => // Won't reach here
      }
    }

    val numString = numStr.toString

    // Check for special values
    val value = numString.toLowerCase match {
      case "inf" => Right(Variant.Float(Double.PositiveInfinity))
      case "-inf" | "inf_neg" => Right(Variant.Float(Double.NegativeInfinity))
      case "nan" => Right(Variant.Float(Double.NaN))
      case _ =>
        try {
          if (isFloat) {
            Right(Variant.Float(numString.toDouble))
          } else {
            Right(Variant.Int(numString.toLong))
          }
        } catch {
          case _: NumberFormatException =>
            Left(ParseError.TokenizeError(
              s"Invalid number format: $numString",
              stream.currentLine,
              stream.currentColumn,
              stream.getContext()
            ))
        }
    }

    value.map(v => (TokenType.Number, v))
  }

  private def parseIdentifier(): ParseResult[(TokenType, Variant)] = {
    val ident = new StringBuilder()

    while (!stream.isEof) {
      val c = stream.getChar()
      if (c.isLetterOrDigit || c == '_') {
        ident.append(c)
      } else {
        stream.saveChar(c)
        return Right((TokenType.Identifier, Variant.String(ident.toString)))
      }
    }

    Right((TokenType.Identifier, Variant.String(ident.toString)))
  }

  private def isHexDigit(c: Char): Boolean = {
    c.isDigit || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
  }
}

private enum NumberState:
  case Start, Integer, Decimal, ExpSign, Exponent, Done
