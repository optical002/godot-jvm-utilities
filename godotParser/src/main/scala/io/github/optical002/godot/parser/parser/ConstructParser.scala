package io.github.optical002.godot.parser.parser

import io.github.optical002.godot.parser.core.*

object ConstructParser {

  def parseConstruct(name: String, tokens: TokenIterator): ParseResult[Variant] =
    name match {
      // Vector types
      case "Vector2" => parseVector2(tokens)
      case "Vector2i" => parseVector2i(tokens)
      case "Vector3" => parseVector3(tokens)
      case "Vector3i" => parseVector3i(tokens)
      case "Vector4" => parseVector4(tokens)
      case "Vector4i" => parseVector4i(tokens)

      // Rect types
      case "Rect2" => parseRect2(tokens)
      case "Rect2i" => parseRect2i(tokens)

      // Transform types
      case "Transform2D" | "Matrix32" => parseTransform2D(tokens) // Matrix32 = Godot 3.x alias
      case "Transform3D" | "Transform" => parseTransform3D(tokens) // Transform = Godot 3.x alias
      case "Basis" | "Matrix3" => parseBasis(tokens) // Matrix3 = Godot 3.x alias
      case "Projection" => parseProjection(tokens)

      // Other math types
      case "Plane" => parsePlane(tokens)
      case "Quaternion" | "Quat" => parseQuaternion(tokens) // Quat = Godot 3.x alias
      case "AABB" | "Rect3" => parseAABB(tokens) // Rect3 = Godot 3.x alias
      case "Color" => parseColorConstruct(tokens)

      // Path and resource types
      case "NodePath" => parseNodePath(tokens)
      case "RID" => parseRID(tokens)
      case "ExtResource" => parseExtResource(tokens)
      case "SubResource" => parseSubResource(tokens)

      // Callable and Signal
      case "Callable" => parseCallable(tokens)
      case "Signal" => parseSignal(tokens)

      // Packed arrays (with Godot 3.x and 2.x compatibility aliases)
      case "PackedByteArray" | "PoolByteArray" | "ByteArray" => parsePackedByteArray(tokens)
      case "PackedInt32Array" | "PackedIntArray" | "PoolIntArray" | "IntArray" => parsePackedInt32Array(tokens)
      case "PackedInt64Array" => parsePackedInt64Array(tokens)
      case "PackedFloat32Array" | "PackedRealArray" | "PoolRealArray" | "FloatArray" => parsePackedFloat32Array(tokens)
      case "PackedFloat64Array" => parsePackedFloat64Array(tokens)
      case "PackedStringArray" | "PoolStringArray" | "StringArray" => parsePackedStringArray(tokens)
      case "PackedVector2Array" | "PoolVector2Array" | "Vector2Array" => parsePackedVector2Array(tokens)
      case "PackedVector3Array" | "PoolVector3Array" | "Vector3Array" => parsePackedVector3Array(tokens)
      case "PackedColorArray" | "PoolColorArray" | "ColorArray" => parsePackedColorArray(tokens)
      case "PackedVector4Array" | "PoolVector4Array" | "Vector4Array" => parsePackedVector4Array(tokens)

      // Typed collections
      case "Array" => parseTypedArray(tokens)
      case "Dictionary" => parseTypedDictionary(tokens)

      case _ => Left(ParseError.SyntaxError(
          s"Unknown construct: $name",
          tokens.currentLine,
          "",
          Some(s"Known construct name"),
          Some(name)
        ))
    }

  private def parseVector2(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 2)
      x <- args(0).asFloat.toRight(invalidArgError("Vector2", 0, "number"))
      y <- args(1).asFloat.toRight(invalidArgError("Vector2", 1, "number"))
    } yield Variant.Vector2(x, y)

  private def parseVector2i(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 2)
      x <- args(0).asInt.toRight(invalidArgError("Vector2i", 0, "integer"))
      y <- args(1).asInt.toRight(invalidArgError("Vector2i", 1, "integer"))
    } yield Variant.Vector2i(x, y)

  private def parseVector3(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 3)
      x <- args(0).asFloat.toRight(invalidArgError("Vector3", 0, "number"))
      y <- args(1).asFloat.toRight(invalidArgError("Vector3", 1, "number"))
      z <- args(2).asFloat.toRight(invalidArgError("Vector3", 2, "number"))
    } yield Variant.Vector3(x, y, z)

  private def parseVector3i(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 3)
      x <- args(0).asInt.toRight(invalidArgError("Vector3i", 0, "integer"))
      y <- args(1).asInt.toRight(invalidArgError("Vector3i", 1, "integer"))
      z <- args(2).asInt.toRight(invalidArgError("Vector3i", 2, "integer"))
    } yield Variant.Vector3i(x, y, z)

  private def parseVector4(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4)
      x <- args(0).asFloat.toRight(invalidArgError("Vector4", 0, "number"))
      y <- args(1).asFloat.toRight(invalidArgError("Vector4", 1, "number"))
      z <- args(2).asFloat.toRight(invalidArgError("Vector4", 2, "number"))
      w <- args(3).asFloat.toRight(invalidArgError("Vector4", 3, "number"))
    } yield Variant.Vector4(x, y, z, w)

  private def parseVector4i(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4)
      x <- args(0).asInt.toRight(invalidArgError("Vector4i", 0, "integer"))
      y <- args(1).asInt.toRight(invalidArgError("Vector4i", 1, "integer"))
      z <- args(2).asInt.toRight(invalidArgError("Vector4i", 2, "integer"))
      w <- args(3).asInt.toRight(invalidArgError("Vector4i", 3, "integer"))
    } yield Variant.Vector4i(x, y, z, w)

  private def parseRect2(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4)
      x <- args(0).asFloat.toRight(invalidArgError("Rect2", 0, "number"))
      y <- args(1).asFloat.toRight(invalidArgError("Rect2", 1, "number"))
      w <- args(2).asFloat.toRight(invalidArgError("Rect2", 2, "number"))
      h <- args(3).asFloat.toRight(invalidArgError("Rect2", 3, "number"))
    } yield Variant.Rect2(x, y, w, h)

  private def parseRect2i(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4)
      x <- args(0).asInt.toRight(invalidArgError("Rect2i", 0, "integer"))
      y <- args(1).asInt.toRight(invalidArgError("Rect2i", 1, "integer"))
      w <- args(2).asInt.toRight(invalidArgError("Rect2i", 2, "integer"))
      h <- args(3).asInt.toRight(invalidArgError("Rect2i", 3, "integer"))
    } yield Variant.Rect2i(x, y, w, h)

  private def parseTransform2D(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 6)
      xx <- args(0).asFloat.toRight(invalidArgError("Transform2D", 0, "number"))
      xy <- args(1).asFloat.toRight(invalidArgError("Transform2D", 1, "number"))
      yx <- args(2).asFloat.toRight(invalidArgError("Transform2D", 2, "number"))
      yy <- args(3).asFloat.toRight(invalidArgError("Transform2D", 3, "number"))
      ox <- args(4).asFloat.toRight(invalidArgError("Transform2D", 4, "number"))
      oy <- args(5).asFloat.toRight(invalidArgError("Transform2D", 5, "number"))
    } yield Variant.Transform2D(xx, xy, yx, yy, ox, oy)

  private def parseTransform3D(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 12)
      nums <-
        args.map(_.asFloat.toRight(invalidArgError("Transform3D", 0, "number"))).foldLeft[ParseResult[Vector[Double]]](
          Right(Vector.empty)
        )((acc, r) =>
          acc.flatMap(v => r.map(n => v :+ n))
        )
    } yield Variant.Transform3D(
      nums(0),
      nums(1),
      nums(2),
      nums(3),
      nums(4),
      nums(5),
      nums(6),
      nums(7),
      nums(8),
      nums(9),
      nums(10),
      nums(11)
    )

  private def parseBasis(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 9)
      nums <- args.map(_.asFloat.toRight(invalidArgError("Basis", 0, "number"))).foldLeft[ParseResult[Vector[Double]]](
        Right(Vector.empty)
      )((acc, r) =>
        acc.flatMap(v => r.map(n => v :+ n))
      )
    } yield Variant.Basis(
      nums(0),
      nums(1),
      nums(2),
      nums(3),
      nums(4),
      nums(5),
      nums(6),
      nums(7),
      nums(8)
    )

  private def parseProjection(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 16)
      nums <-
        args.map(_.asFloat.toRight(invalidArgError("Projection", 0, "number"))).foldLeft[ParseResult[Vector[Double]]](
          Right(Vector.empty)
        )((acc, r) =>
          acc.flatMap(v => r.map(n => v :+ n))
        )
    } yield Variant.Projection(
      nums(0),
      nums(1),
      nums(2),
      nums(3),
      nums(4),
      nums(5),
      nums(6),
      nums(7),
      nums(8),
      nums(9),
      nums(10),
      nums(11),
      nums(12),
      nums(13),
      nums(14),
      nums(15)
    )

  private def parsePlane(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4)
      a <- args(0).asFloat.toRight(invalidArgError("Plane", 0, "number"))
      b <- args(1).asFloat.toRight(invalidArgError("Plane", 1, "number"))
      c <- args(2).asFloat.toRight(invalidArgError("Plane", 2, "number"))
      d <- args(3).asFloat.toRight(invalidArgError("Plane", 3, "number"))
    } yield Variant.Plane(a, b, c, d)

  private def parseQuaternion(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4)
      x <- args(0).asFloat.toRight(invalidArgError("Quaternion", 0, "number"))
      y <- args(1).asFloat.toRight(invalidArgError("Quaternion", 1, "number"))
      z <- args(2).asFloat.toRight(invalidArgError("Quaternion", 2, "number"))
      w <- args(3).asFloat.toRight(invalidArgError("Quaternion", 3, "number"))
    } yield Variant.Quaternion(x, y, z, w)

  private def parseAABB(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 6)
      px <- args(0).asFloat.toRight(invalidArgError("AABB", 0, "number"))
      py <- args(1).asFloat.toRight(invalidArgError("AABB", 1, "number"))
      pz <- args(2).asFloat.toRight(invalidArgError("AABB", 2, "number"))
      sx <- args(3).asFloat.toRight(invalidArgError("AABB", 3, "number"))
      sy <- args(4).asFloat.toRight(invalidArgError("AABB", 4, "number"))
      sz <- args(5).asFloat.toRight(invalidArgError("AABB", 5, "number"))
    } yield Variant.AABB(px, py, pz, sx, sy, sz)

  private def parseColorConstruct(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 4) // Godot requires exactly 4 arguments (r, g, b, a)
      r <- args(0).asFloat.toRight(invalidArgError("Color", 0, "number"))
      g <- args(1).asFloat.toRight(invalidArgError("Color", 1, "number"))
      b <- args(2).asFloat.toRight(invalidArgError("Color", 2, "number"))
      a <- args(3).asFloat.toRight(invalidArgError("Color", 3, "number"))
    } yield Variant.Color(r, g, b, a)

  private def parseNodePath(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 1)
      path <- args(0).asString.orElse(args(0).asStringName).toRight(invalidArgError("NodePath", 0, "string"))
    } yield Variant.NodePath(path)

  private def parseExtResource(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 1)
      id <- args(0).asString.orElse(args(0).asStringName.orElse(args(0).asInt.map(_.toString))).toRight(invalidArgError(
        "ExtResource",
        0,
        "string or int"
      ))
    } yield Variant.Object(ObjectValue.ExtResource(id))

  private def parseSubResource(tokens: TokenIterator): ParseResult[Variant] =
    for {
      args <- parseArguments(tokens, 1)
      id <- args(0).asString.orElse(args(0).asStringName.orElse(args(0).asInt.map(_.toString))).toRight(invalidArgError(
        "SubResource",
        0,
        "string or int"
      ))
    } yield Variant.Object(ObjectValue.SubResource(id))

  private def parseRID(tokens: TokenIterator): ParseResult[Variant] = {
    // RID can be RID() (empty) or RID(uint64_number)
    for {
      args <- parseArguments(tokens, 0, 1) // 0 or 1 arguments
      rid <- args.headOption match {
        case None => Right(0L) // Empty RID()
        case Some(variant) => variant.asInt.toRight(invalidArgError("RID", 0, "integer"))
      }
    } yield Variant.RID(rid)
  }

  private def parseCallable(tokens: TokenIterator): ParseResult[Variant] = {
    // Callable only supports Callable() (empty)
    for {
      args <- parseArguments(tokens, 0)
    } yield Variant.Callable(None, "")
  }

  private def parseSignal(tokens: TokenIterator): ParseResult[Variant] = {
    // Signal only supports Signal() (empty)
    for {
      args <- parseArguments(tokens, 0)
    } yield Variant.Signal(None, "")
  }

  private def parsePackedByteArray(tokens: TokenIterator): ParseResult[Variant] =
    // Expect opening parenthesis
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisOpen) {
      Left(ParseError.SyntaxError(
        "Expected '(' for PackedByteArray",
        tokens.currentLine,
        "",
        Some("("),
        None
      ))
    } else if (!tokens.hasNext) {
      // Check first token after (
      Left(ParseError.SyntaxError(
        "Unexpected EOF in PackedByteArray",
        tokens.currentLine,
        "",
        Some("string or number"),
        Some("EOF")
      ))
    } else {
      val firstToken = tokens.peek()

      firstToken.tokenType match {
        case TokenType.String =>
          // BASE64 encoded string
          tokens.next() // consume string token
          val base64String = firstToken.value.asString.getOrElse("")

          // Decode BASE64
          val bytesResult =
            try
              Right(java.util.Base64.getDecoder.decode(base64String).toVector)
            catch {
              case e: IllegalArgumentException =>
                Left(ParseError.SyntaxError(
                  s"Invalid base64-encoded string: ${e.getMessage}",
                  tokens.currentLine,
                  "",
                  Some("valid base64 string"),
                  Some(base64String)
                ))
            }

          bytesResult.flatMap { bytes =>
            // Expect closing parenthesis
            if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisClose) {
              Left(ParseError.SyntaxError(
                "Expected ')' after base64 string in PackedByteArray",
                tokens.currentLine,
                "",
                Some(")"),
                None
              ))
            } else {
              Right(Variant.PackedByteArray(bytes))
            }
          }

        case TokenType.ParenthesisClose =>
          // Empty array
          tokens.next() // consume closing paren
          Right(Variant.PackedByteArray(Vector.empty))

        case _ =>
          // Comma-separated numbers
          parseCommaSeparatedNumbers(tokens).flatMap { numbers =>
            val bytes = numbers.map(_.toByte)
            Right(Variant.PackedByteArray(bytes))
          }
      }
    }

  private def parsePackedInt32Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedInt32Array").map { numbers =>
      Variant.PackedInt32Array(numbers.map(_.toLong.toInt))
    }

  private def parsePackedInt64Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedInt64Array").map { numbers =>
      Variant.PackedInt64Array(numbers.map(_.toLong))
    }

  private def parsePackedFloat32Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedFloat32Array", allowFloat = true).map { numbers =>
      Variant.PackedFloat32Array(numbers.map(_.toFloat))
    }

  private def parsePackedFloat64Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedFloat64Array", allowFloat = true).map { numbers =>
      Variant.PackedFloat64Array(numbers)
    }

  private def parsePackedStringArray(tokens: TokenIterator): ParseResult[Variant] = {
    // Expect opening parenthesis
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisOpen) {
      return Left(ParseError.SyntaxError(
        "Expected '(' for PackedStringArray",
        tokens.currentLine,
        "",
        Some("("),
        None
      ))
    }

    // Parse comma-separated strings
    val strings = scala.collection.mutable.ArrayBuffer[String]()
    var first = true

    while (tokens.hasNext) {
      if (!first) {
        val token = tokens.peek()
        if (token.tokenType == TokenType.Comma) {
          tokens.next() // consume comma
        } else if (token.tokenType == TokenType.ParenthesisClose) {
          tokens.next() // consume closing paren
          return Right(Variant.PackedStringArray(strings.toVector))
        } else {
          return Left(ParseError.SyntaxError(
            "Expected ',' or ')' in PackedStringArray",
            tokens.currentLine,
            "",
            Some("',' or ')'"),
            Some(token.tokenType.toString)
          ))
        }
      }

      val token = tokens.peek()
      if (first && token.tokenType == TokenType.ParenthesisClose) {
        tokens.next() // consume closing paren
        return Right(Variant.PackedStringArray(Vector.empty))
      }

      if (token.tokenType != TokenType.String && token.tokenType != TokenType.StringName) {
        return Left(ParseError.SyntaxError(
          "Expected string in PackedStringArray",
          tokens.currentLine,
          "",
          Some("string"),
          Some(token.tokenType.toString)
        ))
      }

      tokens.next() // consume string
      token.value.asString.orElse(token.value.asStringName) match {
        case Some(str) => strings += str
        case None =>
          return Left(ParseError.SyntaxError(
            "Expected string value in PackedStringArray",
            tokens.currentLine,
            "",
            Some("string"),
            None
          ))
      }

      first = false
    }

    Left(ParseError.SyntaxError(
      "Unexpected EOF in PackedStringArray",
      tokens.currentLine,
      "",
      Some("')'"),
      Some("EOF")
    ))
  }

  private def parsePackedVector2Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedVector2Array", allowFloat = true).flatMap { numbers =>
      if (numbers.length % 2 != 0) {
        Left(ParseError.SyntaxError(
          s"PackedVector2Array requires an even number of values (got ${numbers.length})",
          tokens.currentLine,
          "",
          Some("even number of values"),
          Some(s"${numbers.length} values")
        ))
      } else {
        val vectors = numbers.grouped(2).map { case Vector(x, y, _*) => (x, y) }.toVector
        Right(Variant.PackedVector2Array(vectors))
      }
    }

  private def parsePackedVector3Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedVector3Array", allowFloat = true).flatMap { numbers =>
      if (numbers.length % 3 != 0) {
        Left(ParseError.SyntaxError(
          s"PackedVector3Array requires a multiple of 3 values (got ${numbers.length})",
          tokens.currentLine,
          "",
          Some("multiple of 3 values"),
          Some(s"${numbers.length} values")
        ))
      } else {
        val vectors = numbers.grouped(3).map { case Vector(x, y, z, _*) => (x, y, z) }.toVector
        Right(Variant.PackedVector3Array(vectors))
      }
    }

  private def parsePackedColorArray(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedColorArray", allowFloat = true).flatMap { numbers =>
      if (numbers.length % 4 != 0) {
        Left(ParseError.SyntaxError(
          s"PackedColorArray requires a multiple of 4 values (got ${numbers.length})",
          tokens.currentLine,
          "",
          Some("multiple of 4 values"),
          Some(s"${numbers.length} values")
        ))
      } else {
        val colors = numbers.grouped(4).map { case Vector(r, g, b, a, _*) => (r, g, b, a) }.toVector
        Right(Variant.PackedColorArray(colors))
      }
    }

  private def parsePackedVector4Array(tokens: TokenIterator): ParseResult[Variant] =
    parsePackedArray(tokens, "PackedVector4Array", allowFloat = true).flatMap { numbers =>
      if (numbers.length % 4 != 0) {
        Left(ParseError.SyntaxError(
          s"PackedVector4Array requires a multiple of 4 values (got ${numbers.length})",
          tokens.currentLine,
          "",
          Some("multiple of 4 values"),
          Some(s"${numbers.length} values")
        ))
      } else {
        val vectors = numbers.grouped(4).map { case Vector(x, y, z, w, _*) => (x, y, z, w) }.toVector
        Right(Variant.PackedVector4Array(vectors))
      }
    }

  private def parseTypedArray(tokens: TokenIterator): ParseResult[Variant] =
    // Array[type]([...]) - simplified for now, just return untyped array
    for {
      arr <- parseArrayArgument(tokens)
    } yield arr

  private def parseTypedDictionary(tokens: TokenIterator): ParseResult[Variant] =
    // Dictionary[keyType, valueType]({...}) - simplified for now, just return untyped dict
    for {
      dict <- parseDictionaryArgument(tokens)
    } yield dict

  private def parsePackedArray(
    tokens: TokenIterator,
    arrayType: String,
    allowFloat: Boolean = false
  ): ParseResult[Vector[Double]] = {
    // Expect opening parenthesis
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisOpen) {
      return Left(ParseError.SyntaxError(
        s"Expected '(' for $arrayType",
        tokens.currentLine,
        "",
        Some("("),
        None
      ))
    }

    val numbers = scala.collection.mutable.ArrayBuffer[Double]()
    var first = true

    while (tokens.hasNext) {
      if (!first) {
        val token = tokens.peek()
        if (token.tokenType == TokenType.Comma) {
          tokens.next() // consume comma
        } else if (token.tokenType == TokenType.ParenthesisClose) {
          tokens.next() // consume closing paren
          return Right(numbers.toVector)
        } else {
          return Left(ParseError.SyntaxError(
            s"Expected ',' or ')' in $arrayType",
            tokens.currentLine,
            "",
            Some("',' or ')'"),
            Some(token.tokenType.toString)
          ))
        }
      }

      val token = tokens.peek()
      if (first && token.tokenType == TokenType.ParenthesisClose) {
        tokens.next() // consume closing paren
        return Right(Vector.empty)
      }

      if (token.tokenType != TokenType.Number) {
        return Left(ParseError.SyntaxError(
          s"Expected number in $arrayType",
          tokens.currentLine,
          "",
          Some("number"),
          Some(token.tokenType.toString)
        ))
      }

      tokens.next() // consume number
      if (allowFloat) {
        token.value.asFloat match {
          case Some(num) => numbers += num
          case None =>
            return Left(ParseError.SyntaxError(
              s"Expected numeric value in $arrayType",
              tokens.currentLine,
              "",
              Some("number"),
              None
            ))
        }
      } else {
        token.value.asInt match {
          case Some(num) => numbers += num.toDouble
          case None =>
            return Left(ParseError.SyntaxError(
              s"Expected integer value in $arrayType",
              tokens.currentLine,
              "",
              Some("integer"),
              None
            ))
        }
      }

      first = false
    }

    Left(ParseError.SyntaxError(
      s"Unexpected EOF in $arrayType",
      tokens.currentLine,
      "",
      Some("')'"),
      Some("EOF")
    ))
  }

  private def parseCommaSeparatedNumbers(tokens: TokenIterator): ParseResult[Vector[Long]] = {
    val numbers = scala.collection.mutable.ArrayBuffer[Long]()
    var first = true

    while (tokens.hasNext) {
      if (!first) {
        val token = tokens.peek()
        if (token.tokenType == TokenType.Comma) {
          tokens.next() // consume comma
        } else if (token.tokenType == TokenType.ParenthesisClose) {
          tokens.next() // consume closing paren
          return Right(numbers.toVector)
        } else {
          return Left(ParseError.SyntaxError(
            "Expected ',' or ')' after number",
            tokens.currentLine,
            "",
            Some("',' or ')'"),
            Some(token.tokenType.toString)
          ))
        }
      }

      if (!tokens.hasNext) {
        return Left(ParseError.SyntaxError(
          "Unexpected EOF while parsing numbers",
          tokens.currentLine,
          "",
          Some("number"),
          Some("EOF")
        ))
      }

      val token = tokens.peek()
      if (token.tokenType != TokenType.Number) {
        return Left(ParseError.SyntaxError(
          "Expected number",
          tokens.currentLine,
          "",
          Some("number"),
          Some(token.tokenType.toString)
        ))
      }

      tokens.next() // consume number
      token.value.asInt match {
        case Some(num) => numbers += num
        case None =>
          return Left(ParseError.SyntaxError(
            "Expected numeric value",
            tokens.currentLine,
            "",
            Some("number"),
            None
          ))
      }

      first = false
    }

    Left(ParseError.SyntaxError(
      "Unexpected EOF while parsing numbers",
      tokens.currentLine,
      "",
      Some("')'"),
      Some("EOF")
    ))
  }

  private def parseArguments(tokens: TokenIterator, expectedCount: Int): ParseResult[Vector[Variant]] =
    parseArguments(tokens, expectedCount, expectedCount)

  private def parseArguments(tokens: TokenIterator, minCount: Int, maxCount: Int): ParseResult[Vector[Variant]] = {
    val args = scala.collection.mutable.ArrayBuffer[Variant]()

    // Expect opening parenthesis
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisOpen) {
      return Left(ParseError.SyntaxError(
        "Expected '(' to start construct arguments",
        tokens.currentLine,
        "",
        Some("("),
        None
      ))
    }

    // Parse arguments
    while (tokens.hasNext) {
      val token = tokens.peek()

      if (token.tokenType == TokenType.ParenthesisClose) {
        tokens.next() // consume closing paren
        if (args.length < minCount || args.length > maxCount) {
          val expectedRange = if (minCount == maxCount) s"$minCount" else s"$minCount-$maxCount"
          return Left(ParseError.SyntaxError(
            s"Expected $expectedRange arguments, got ${args.length}",
            tokens.currentLine,
            "",
            Some(s"$expectedRange arguments"),
            Some(s"${args.length} arguments")
          ))
        }
        return Right(args.toVector)
      }

      // Parse value
      VariantParser.parseValue(tokens) match {
        case Right(value) =>
          args += value

          // Check for comma or closing paren
          if (tokens.hasNext) {
            val next = tokens.peek()
            if (next.tokenType == TokenType.Comma) {
              tokens.next() // consume comma
            } else if (next.tokenType != TokenType.ParenthesisClose) {
              return Left(ParseError.SyntaxError(
                "Expected ',' or ')' after argument",
                tokens.currentLine,
                "",
                Some("',' or ')'"),
                Some(next.tokenType.toString)
              ))
            }
          }

        case Left(err) => return Left(err)
      }
    }

    Left(ParseError.SyntaxError(
      "Unexpected EOF in construct arguments",
      tokens.currentLine,
      "",
      Some("')'"),
      Some("EOF")
    ))
  }

  private def parseArrayArgument(tokens: TokenIterator): ParseResult[Variant.Array] = {
    // Expect opening parenthesis
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisOpen) {
      return Left(ParseError.SyntaxError(
        "Expected '(' for packed array argument",
        tokens.currentLine,
        "",
        Some("("),
        None
      ))
    }

    // Parse array
    VariantParser.parseValue(tokens) match {
      case Right(Variant.Array(elements, typed)) =>
        // Expect closing parenthesis
        if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisClose) {
          return Left(ParseError.SyntaxError(
            "Expected ')' after packed array argument",
            tokens.currentLine,
            "",
            Some(")"),
            None
          ))
        }
        Right(Variant.Array(elements, typed))

      case Right(other) =>
        Left(ParseError.SyntaxError(
          s"Expected array argument, got ${other.getClass.getSimpleName}",
          tokens.currentLine,
          s"Received value: $other",
          Some("Array"),
          Some(other.getClass.getSimpleName)
        ))

      case Left(err) => Left(err)
    }
  }

  private def parseDictionaryArgument(tokens: TokenIterator): ParseResult[Variant.Dictionary] = {
    // Expect opening parenthesis
    if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisOpen) {
      return Left(ParseError.SyntaxError(
        "Expected '(' for dictionary argument",
        tokens.currentLine,
        "",
        Some("("),
        None
      ))
    }

    // Parse dictionary
    VariantParser.parseValue(tokens) match {
      case Right(Variant.Dictionary(entries, typed)) =>
        // Expect closing parenthesis
        if (!tokens.hasNext || tokens.next().tokenType != TokenType.ParenthesisClose) {
          return Left(ParseError.SyntaxError(
            "Expected ')' after dictionary argument",
            tokens.currentLine,
            "",
            Some(")"),
            None
          ))
        }
        Right(Variant.Dictionary(entries, typed))

      case Right(other) =>
        Left(ParseError.SyntaxError(
          s"Expected dictionary argument, got ${other.getClass.getSimpleName}",
          tokens.currentLine,
          "",
          Some("Dictionary"),
          Some(other.getClass.getSimpleName)
        ))

      case Left(err) => Left(err)
    }
  }

  private def invalidArgError(construct: String, index: Int, expected: String): ParseError =
    ParseError.SyntaxError(
      s"Invalid argument $index for $construct: expected $expected",
      0,
      "",
      Some(expected),
      None
    )
}

class TokenIterator(tokens: Vector[Token]) {
  private var position = 0

  def hasNext: Boolean = position < tokens.length

  def next(): Token = {
    val token = tokens(position)
    position += 1
    token
  }

  def peek(): Token = tokens(position)

  def currentLine: Int = if (position < tokens.length) tokens(position).line else 0
}
