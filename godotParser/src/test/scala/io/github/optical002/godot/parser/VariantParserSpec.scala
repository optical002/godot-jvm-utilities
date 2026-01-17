package io.github.optical002.godot.parser

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.parser.{VariantParser, TokenIterator}
import io.github.optical002.godot.parser.tokenizer.{CharStream, VariantTokenizer}
import munit.FunSuite

class VariantParserSpec extends FunSuite {

  // Helper to parse a value from a string
  def parseValue(input: String): ParseResult[Variant] = {
    val stream = CharStream(input)
    val tokenizer = VariantTokenizer(stream)

    // Collect tokens
    val tokensBuilder = Vector.newBuilder[Token]
    var continue = true
    while (continue)
      tokenizer.getToken() match {
        case Right(token) =>
          if (token.tokenType == TokenType.EOF) {
            continue = false
          } else {
            tokensBuilder += token
          }
        case Left(err) => return Left(err)
      }

    val tokens = new TokenIterator(tokensBuilder.result())
    VariantParser.parseValue(tokens)
  }

  // 1. Nil
  test("parse null/nil") {
    val result = parseValue("null")
    assertEquals(result, Right(Variant.Nil))
  }

  // 2. Bool
  test("parse true") {
    val result = parseValue("true")
    assertEquals(result, Right(Variant.Bool(true)))
  }

  test("parse false") {
    val result = parseValue("false")
    assertEquals(result, Right(Variant.Bool(false)))
  }

  // 3. Int
  test("parse integer") {
    val result = parseValue("42")
    assertEquals(result, Right(Variant.Int(42)))
  }

  // 4. Float
  test("parse float") {
    val result = parseValue("3.14159")
    assertEquals(result, Right(Variant.Float(3.14159)))
  }

  // 5. String
  test("parse string") {
    val result = parseValue("\"Hello World\"")
    assertEquals(result, Right(Variant.String("Hello World")))
  }

  // 6. StringName
  test("parse string name") {
    val result = parseValue("&\"MyNode\"")
    assertEquals(result, Right(Variant.StringName("MyNode")))
  }

  // 7. Vector2
  test("parse Vector2") {
    val result = parseValue("Vector2(10.5, 20.0)")
    assertEquals(result, Right(Variant.Vector2(10.5, 20.0)))
  }

  // 8. Vector2i
  test("parse Vector2i") {
    val result = parseValue("Vector2i(10, 20)")
    assertEquals(result, Right(Variant.Vector2i(10, 20)))
  }

  // 9. Rect2
  test("parse Rect2") {
    val result = parseValue("Rect2(0, 0, 100, 50)")
    assertEquals(result, Right(Variant.Rect2(0, 0, 100, 50)))
  }

  // 10. Rect2i
  test("parse Rect2i") {
    val result = parseValue("Rect2i(0, 0, 100, 50)")
    assertEquals(result, Right(Variant.Rect2i(0, 0, 100, 50)))
  }

  // 11. Vector3
  test("parse Vector3") {
    val result = parseValue("Vector3(1, 2, 3)")
    assertEquals(result, Right(Variant.Vector3(1, 2, 3)))
  }

  // 12. Vector3i
  test("parse Vector3i") {
    val result = parseValue("Vector3i(1, 2, 3)")
    assertEquals(result, Right(Variant.Vector3i(1, 2, 3)))
  }

  // 13. Transform2D
  test("parse Transform2D") {
    val result = parseValue("Transform2D(1, 0, 0, 1, 0, 0)")
    assertEquals(result, Right(Variant.Transform2D(1, 0, 0, 1, 0, 0)))
  }

  // 14. Plane
  test("parse Plane") {
    val result = parseValue("Plane(0, 1, 0, 5)")
    assertEquals(result, Right(Variant.Plane(0, 1, 0, 5)))
  }

  // 15. Quaternion
  test("parse Quaternion") {
    val result = parseValue("Quaternion(0, 0, 0, 1)")
    assertEquals(result, Right(Variant.Quaternion(0, 0, 0, 1)))
  }

  // 16. AABB
  test("parse AABB") {
    val result = parseValue("AABB(0, 0, 0, 10, 10, 10)")
    assertEquals(result, Right(Variant.AABB(0, 0, 0, 10, 10, 10)))
  }

  // 17. Basis
  test("parse Basis") {
    val result = parseValue("Basis(1, 0, 0, 0, 1, 0, 0, 0, 1)")
    assertEquals(result, Right(Variant.Basis(1, 0, 0, 0, 1, 0, 0, 0, 1)))
  }

  // 18. Vector4
  test("parse Vector4") {
    val result = parseValue("Vector4(1, 2, 3, 4)")
    assertEquals(result, Right(Variant.Vector4(1, 2, 3, 4)))
  }

  // 19. Vector4i
  test("parse Vector4i") {
    val result = parseValue("Vector4i(1, 2, 3, 4)")
    assertEquals(result, Right(Variant.Vector4i(1, 2, 3, 4)))
  }

  // 20. Transform3D
  test("parse Transform3D") {
    val result = parseValue("Transform3D(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0)")
    assertEquals(result, Right(Variant.Transform3D(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0)))
  }

  // 21. Projection
  test("parse Projection") {
    val result = parseValue("Projection(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1)")
    assertEquals(result, Right(Variant.Projection(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1)))
  }

  // 22. Color - from hex
  test("parse Color from hex") {
    val result = parseValue("#ff0000")
    result match {
      case Right(Variant.Color(r, g, b, a)) =>
        assertEquals(r, 1.0, 0.01)
        assertEquals(g, 0.0, 0.01)
        assertEquals(b, 0.0, 0.01)
        assertEquals(a, 1.0, 0.01)
      case other => fail(s"Expected Color, got $other")
    }
  }

  // 22b. Color - from construct
  test("parse Color from construct") {
    val result = parseValue("Color(1.0, 0.5, 0.0, 1.0)")
    assertEquals(result, Right(Variant.Color(1.0, 0.5, 0.0, 1.0)))
  }

  // 23. NodePath
  test("parse NodePath") {
    val result = parseValue("NodePath(\"path/to/node\")")
    assertEquals(result, Right(Variant.NodePath("path/to/node")))
  }

  // 24. RID
  test("parse RID") {
    val result = parseValue("RID(12345)")
    assertEquals(result, Right(Variant.RID(12345)))
  }

  // 24b. RID (empty)
  test("parse empty RID") {
    val result = parseValue("RID()")
    assertEquals(result, Right(Variant.RID(0)))
  }

  // 25. Object - ExtResource
  test("parse ExtResource") {
    val result = parseValue("ExtResource(\"1\")")
    assertEquals(result, Right(Variant.Object(ObjectValue.ExtResource("1"))))
  }

  // 25b. Object - SubResource
  test("parse SubResource") {
    val result = parseValue("SubResource(\"1\")")
    assertEquals(result, Right(Variant.Object(ObjectValue.SubResource("1"))))
  }

  // 26. Callable (empty)
  test("parse Callable") {
    val result = parseValue("Callable()")
    assertEquals(result, Right(Variant.Callable(None, "")))
  }

  // 27. Signal (empty)
  test("parse Signal") {
    val result = parseValue("Signal()")
    assertEquals(result, Right(Variant.Signal(None, "")))
  }

  // 28. Dictionary
  test("parse Dictionary") {
    val result = parseValue("{\"key1\": 42, \"key2\": \"value\"}")
    result match {
      case Right(Variant.Dictionary(entries, typed)) =>
        assertEquals(typed, None)
        assertEquals(entries.get("key1"), Some(Variant.Int(42)))
        assertEquals(entries.get("key2"), Some(Variant.String("value")))
      case other => fail(s"Expected Dictionary, got $other")
    }
  }

  // 29. Array
  test("parse Array") {
    val result = parseValue("[1, 2, 3]")
    assertEquals(result, Right(Variant.Array(Vector(Variant.Int(1), Variant.Int(2), Variant.Int(3)), None)))
  }

  // 30. PackedByteArray - numeric format
  test("parse PackedByteArray with numbers") {
    val result = parseValue("PackedByteArray(1, 2, 3)")
    assertEquals(result, Right(Variant.PackedByteArray(Vector(1, 2, 3))))
  }

  // 30b. PackedByteArray - BASE64 format
  test("parse PackedByteArray with BASE64") {
    // "SGVsbG8=" is BASE64 for "Hello" which is bytes [72, 101, 108, 108, 111]
    val result = parseValue("PackedByteArray(\"SGVsbG8=\")")
    assertEquals(result, Right(Variant.PackedByteArray(Vector(72, 101, 108, 108, 111))))
  }

  // 30c. PackedByteArray - empty
  test("parse empty PackedByteArray") {
    val result = parseValue("PackedByteArray()")
    assertEquals(result, Right(Variant.PackedByteArray(Vector.empty)))
  }

  // 30d. PackedByteArray - longer BASE64 (typical in Godot files)
  test("parse PackedByteArray with longer BASE64") {
    // This BASE64 string decodes to a sequence of bytes
    val base64 = "AQIDBA==" // Decodes to [1, 2, 3, 4]
    val result = parseValue(s"PackedByteArray(\"$base64\")")
    assertEquals(result, Right(Variant.PackedByteArray(Vector(1, 2, 3, 4))))
  }

  // 30e. PackedByteArray - invalid BASE64 should fail gracefully
  test("parse PackedByteArray with invalid BASE64") {
    val result = parseValue("PackedByteArray(\"!!!invalid!!!\")")
    assert(result.isLeft, "Invalid BASE64 should return an error")
  }

  // 31. PackedInt32Array
  test("parse PackedInt32Array") {
    val result = parseValue("PackedInt32Array(10, 20, 30)")
    assertEquals(result, Right(Variant.PackedInt32Array(Vector(10, 20, 30))))
  }

  // 32. PackedInt64Array
  test("parse PackedInt64Array") {
    val result = parseValue("PackedInt64Array(100, 200, 300)")
    assertEquals(result, Right(Variant.PackedInt64Array(Vector(100, 200, 300))))
  }

  // 33. PackedFloat32Array
  test("parse PackedFloat32Array") {
    val result = parseValue("PackedFloat32Array(1.0, 2.0, 3.0)")
    assertEquals(result, Right(Variant.PackedFloat32Array(Vector(1.0f, 2.0f, 3.0f))))
  }

  // 34. PackedFloat64Array
  test("parse PackedFloat64Array") {
    val result = parseValue("PackedFloat64Array(1.5, 2.5, 3.5)")
    assertEquals(result, Right(Variant.PackedFloat64Array(Vector(1.5, 2.5, 3.5))))
  }

  // 35. PackedStringArray
  test("parse PackedStringArray") {
    val result = parseValue("PackedStringArray(\"hello\", \"world\")")
    assertEquals(result, Right(Variant.PackedStringArray(Vector("hello", "world"))))
  }

  // 36. PackedVector2Array (flattened)
  test("parse PackedVector2Array") {
    val result = parseValue("PackedVector2Array(1, 2, 3, 4)")
    assertEquals(result, Right(Variant.PackedVector2Array(Vector((1.0, 2.0), (3.0, 4.0)))))
  }

  // 37. PackedVector3Array (flattened)
  test("parse PackedVector3Array") {
    val result = parseValue("PackedVector3Array(1, 2, 3, 4, 5, 6)")
    assertEquals(result, Right(Variant.PackedVector3Array(Vector((1.0, 2.0, 3.0), (4.0, 5.0, 6.0)))))
  }

  // 38. PackedColorArray (flattened)
  test("parse PackedColorArray") {
    val result = parseValue("PackedColorArray(1, 0, 0, 1, 0, 1, 0, 1)")
    assertEquals(result, Right(Variant.PackedColorArray(Vector((1.0, 0.0, 0.0, 1.0), (0.0, 1.0, 0.0, 1.0)))))
  }

  // 39. PackedVector4Array (flattened)
  test("parse PackedVector4Array") {
    val result = parseValue("PackedVector4Array(1, 2, 3, 4, 5, 6, 7, 8)")
    assertEquals(result, Right(Variant.PackedVector4Array(Vector((1.0, 2.0, 3.0, 4.0), (5.0, 6.0, 7.0, 8.0)))))
  }
}
