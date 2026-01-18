# Godot Variant Parser Guide

## Godot Source Reference

### Official Variant Type Definition

**Location:** `/home/optick/work/godot/core/variant/variant.h` (lines 95-145)

This is the authoritative source for all Variant types in Godot Engine. The `Variant::Type` enum defines all possible variant types that can exist in Godot's type system.

### Related Files

- **Variant Parser Implementation:** `/home/optick/work/godot/core/variant/variant_parser.cpp`
  - Contains the C++ parsing logic for text format files
  - Handles `.tscn`, `.tres`, `.godot`, `.cfg`, and `.import` files

- **Resource Format Text:** `/home/optick/work/godot/scene/resources/resource_format_text.cpp`
  - Higher-level resource loading using the variant parser
  - Handles scene and resource assembly

---

## All Godot Variant Types

### Complete Type List (42 types)

Based on `Variant::Type` enum from `godot/core/variant/variant.h`:

#### 1. NIL (1 type)
- `NIL` - Null/nil value (Scala: `Variant.Nil`)

#### 2. Atomic Types (4 types)
- `BOOL` - Boolean value (Scala: `Variant.Bool(value)`)
- `INT` - 64-bit signed integer (Scala: `Variant.Int(value)`)
- `FLOAT` - Double-precision floating point (Scala: `Variant.Float(value)`)
- `STRING` - UTF-8 string (Scala: `Variant.String(value)`)

#### 3. Math Types - 2D (4 types)
- `VECTOR2` - 2D vector with float components (Scala: `Variant.Vector2(x, y)`)
- `VECTOR2I` - 2D vector with integer components (Scala: `Variant.Vector2i(x, y)`)
- `RECT2` - 2D rectangle with float components (Scala: `Variant.Rect2(x, y, width, height)`)
- `RECT2I` - 2D rectangle with integer components (Scala: `Variant.Rect2i(x, y, width, height)`)

#### 4. Math Types - 3D (3 types)
- `VECTOR3` - 3D vector with float components (Scala: `Variant.Vector3(x, y, z)`)
- `VECTOR3I` - 3D vector with integer components (Scala: `Variant.Vector3i(x, y, z)`)
- `PLANE` - 3D plane (Scala: `Variant.Plane(a, b, c, d)`)

#### 5. Math Types - 4D (2 types)
- `VECTOR4` - 4D vector with float components (Scala: `Variant.Vector4(x, y, z, w)`)
- `VECTOR4I` - 4D vector with integer components (Scala: `Variant.Vector4i(x, y, z, w)`)

#### 6. Math Types - Transforms (5 types)
- `TRANSFORM2D` - 2D transformation matrix (Scala: `Variant.Transform2D(xx, xy, yx, yy, ox, oy)`)
- `QUATERNION` - Rotation quaternion (Scala: `Variant.Quaternion(x, y, z, w)`)
- `AABB` - Axis-aligned bounding box (Scala: `Variant.AABB(px, py, pz, sx, sy, sz)`)
- `BASIS` - 3x3 basis matrix (Scala: `Variant.Basis(9 components)`)
- `TRANSFORM3D` - 3D transformation matrix (Scala: `Variant.Transform3D(12 components)`)
- `PROJECTION` - 4x4 projection matrix (Scala: `Variant.Projection(16 components)`)

#### 7. Misc Types (6 types)
- `COLOR` - RGBA color (Scala: `Variant.Color(r, g, b, a)`)
- `STRING_NAME` - Optimized string for identifiers (Scala: `Variant.StringName(value)`)
- `NODE_PATH` - Path to a node in scene tree (Scala: `Variant.NodePath(path)`)
- `RID` - Resource ID (Scala: `Variant.RID(id)`)
- `OBJECT` - Reference to an object (Scala: `Variant.Object(ObjectValue)`)
- `CALLABLE` - Callable function reference (Scala: `Variant.Callable(target, method)`)
- `SIGNAL` - Signal reference (Scala: `Variant.Signal(source, signalName)`)

#### 8. Collections (2 types)
- `DICTIONARY` - Key-value map (Scala: `Variant.Dictionary(entries, typed)`)
- `ARRAY` - Dynamic array (Scala: `Variant.Array(elements, typed)`)

#### 9. Packed Arrays (10 types)
- `PACKED_BYTE_ARRAY` - Array of bytes (Scala: `Variant.PackedByteArray(data)`)
- `PACKED_INT32_ARRAY` - Array of 32-bit integers (Scala: `Variant.PackedInt32Array(data)`)
- `PACKED_INT64_ARRAY` - Array of 64-bit integers (Scala: `Variant.PackedInt64Array(data)`)
- `PACKED_FLOAT32_ARRAY` - Array of 32-bit floats (Scala: `Variant.PackedFloat32Array(data)`)
- `PACKED_FLOAT64_ARRAY` - Array of 64-bit floats (Scala: `Variant.PackedFloat64Array(data)`)
- `PACKED_STRING_ARRAY` - Array of strings (Scala: `Variant.PackedStringArray(data)`)
- `PACKED_VECTOR2_ARRAY` - Array of Vector2 (Scala: `Variant.PackedVector2Array(data)`)
- `PACKED_VECTOR3_ARRAY` - Array of Vector3 (Scala: `Variant.PackedVector3Array(data)`)
- `PACKED_COLOR_ARRAY` - Array of colors (Scala: `Variant.PackedColorArray(data)`)
- `PACKED_VECTOR4_ARRAY` - Array of Vector4 (Scala: `Variant.PackedVector4Array(data)`)

**Total:** 42 types (`VARIANT_MAX` = 42)

### Special Object Types

The `OBJECT` variant type has sub-types (defined as `ObjectValue` in Scala):

- `ExtResource(id)` - External resource reference (from another file)
- `SubResource(id)` - Internal resource reference (within same file)
- `Resource(path)` - Resource by path
- `Null` - Null object reference

---

## Scala Variant Parser Architecture

### Three-Layer Design

```
Input String → [Tokenizer] → Tokens → [Parser] → Variants → [Assembler] → Typed Output
               (Lexical)              (Syntactic)            (Semantic)
```

### Layer 1: Tokenizer (Lexical Analysis)

**Location:** `godotParser/src/main/scala/io/github/optical002/godot/parser/tokenizer/`

**Files:**
- `CharStream.scala` - Character stream with line/column tracking
- `VariantTokenizer.scala` - Converts raw text into tokens

**Token Types:**
```scala
enum TokenType:
  case CurlyBracketOpen, CurlyBracketClose    // { }
  case BracketOpen, BracketClose               // [ ]
  case ParenthesisOpen, ParenthesisClose       // ( )
  case Identifier                               // abc, true, false
  case String, StringName                       // "text", &"name"
  case Number                                   // 123, 45.67, 1e10
  case Color                                    // #ff0000
  case Colon, Comma, Period, Equal             // : , . =
  case EOF, Error
```

**What the Tokenizer Does:**
- Reads characters one by one from `CharStream`
- Identifies token boundaries (e.g., where a number ends and a comma begins)
- Converts escape sequences in strings (`\n`, `\t`, `\u####`)
- Parses hex colors (`#RGB`, `#RRGGBB`, `#RRGGBBAA`)
- Handles scientific notation for numbers (`1e10`, `3.14e-5`)
- Skips whitespace and comments (`;` to end of line)
- Produces a `Token` with: `(TokenType, Variant value, line, column)`

**Example:**
```
Input: position = Vector2(10.5, 20)
Tokens:
  1. Identifier("position")
  2. Equal
  3. Identifier("Vector2")
  4. ParenthesisOpen
  5. Number(10.5)
  6. Comma
  7. Number(20)
  8. ParenthesisClose
```

### Layer 2: Parser (Syntactic Analysis)

**Location:** `godotParser/src/main/scala/io/github/optical002/godot/parser/parser/`

**Files:**
- `VariantParser.scala` - Main parsing logic for tags and values
- `ConstructParser.scala` - Parses special constructs like `Vector2()`, `Color()`, etc.

**What the Parser Does:**
- Consumes tokens from the tokenizer
- Builds structured data: `Tag` objects and `Variant` values
- Handles recursive structures (arrays containing arrays, etc.)
- Recognizes constructs by pattern: `Identifier` + `(` → construct

**Key Functions:**

1. **`parseTag()`** - Parses `[tag_name field1=value1 field2=value2]`
   ```scala
   [gd_scene load_steps=2 format=4]
   →
   Tag(
     name = "gd_scene",
     fields = Map("load_steps" → Int(2), "format" → Int(4)),
     line = 1
   )
   ```

2. **`parseValue()`** - Parses any variant value
   - Direct values: strings, numbers, colors from tokens
   - Keywords: `true`, `false`, `null`
   - Constructs: `Vector2(x, y)`, `Color(r, g, b, a)`
   - Arrays: `[1, 2, 3]`
   - Dictionaries: `{key1: value1, key2: value2}`

3. **`parseArray()`** - Parses `[element1, element2, ...]`

4. **`parseDictionary()`** - Parses `{key1: val1, key2: val2}`

5. **`ConstructParser.parseConstruct()`** - Parses special constructs:
   - Math types: `Vector2(x, y)`, `Transform3D(...)`
   - Resources: `ExtResource("id")`, `SubResource("id")`
   - Packed arrays: `PackedInt32Array(1, 2, 3)`
   - BASE64 arrays: `PackedByteArray("base64string")`

**Example:**
```
Input tokens: [Vector2, (, 10.5, ,, 20, )]
Output: Variant.Vector2(10.5, 20.0)

Input tokens: [PackedInt32Array, (, 1, ,, 2, ,, 3, )]
Output: Variant.PackedInt32Array(Vector(1, 2, 3))
```

### Layer 3: Assembler (Semantic Analysis)

**Location:** `godotParser/src/main/scala/io/github/optical002/godot/parser/assembler/`

**Files:**
- `SceneAssembler.scala` - Assembles `.tscn` files into `PackedScene`
- `ResourceAssembler.scala` - Assembles `.tres` files into `TextResource`
- `ConfigAssembler.scala` - Assembles `.godot`/`.cfg`/`.import` into `ConfigFile`

**What the Assembler Does:**
- Takes a vector of `Tag` objects from the parser
- Validates file structure (correct header, required fields)
- Groups related tags (resources, nodes, connections)
- Builds strongly-typed output structures
- Extracts metadata (format version, UIDs, load steps)

**Example - SceneAssembler:**
```scala
Input Tags:
  1. Tag("gd_scene", Map("load_steps" → 2, "format" → 4, "uid" → "..."))
  2. Tag("ext_resource", Map("type" → "Script", "path" → "...", "id" → "1"))
  3. Tag("node", Map("name" → "Player", "type" → "CharacterBody2D", ...))

Output: PackedScene(
  loadSteps = 2,
  format = 4,
  uid = Some("..."),
  extResources = Vector(ExtResource("1", "Script", "...", ...)),
  nodes = Vector(NodeData("Player", Some("CharacterBody2D"), ...)),
  ...
)
```

---

## How to Use: Parsing Raw Text into Variants

### Public API

**Location:** `godotParser/src/main/scala/io/github/optical002/godot/parser/Parser.scala`

The main entry point is the `Parser` object with three methods:

```scala
object Parser {
  // Parse a .tscn file
  def parseTscn(content: String): ParseResult[PackedScene]

  // Parse a .tres file
  def parseTres(content: String): ParseResult[TextResource]

  // Parse .godot/.cfg/.import files
  def parseConfig(content: String): ParseResult[ConfigFile]
}
```

All methods return `ParseResult[T]` which is a type alias:
```scala
type ParseResult[T] = Either[ParseError, T]
```

### Usage Examples

#### Example 1: Parse a Simple .tscn File

```scala
import io.github.optical002.godot.parser.Parser
import io.github.optical002.godot.parser.model.PackedScene

val tscnContent = """
[gd_scene load_steps=2 format=4]

[node name="Root" type="Node2D"]
position = Vector2(100, 200)
"""

Parser.parseTscn(tscnContent) match {
  case Right(scene: PackedScene) =>
    println(s"Format: ${scene.format}")
    println(s"Load steps: ${scene.loadSteps}")
    println(s"Nodes: ${scene.nodes.length}")

    scene.nodes.foreach { node =>
      println(s"Node: ${node.name} (${node.nodeType})")
      node.properties.foreach { case (key, value) =>
        println(s"  $key = $value")
      }
    }

  case Left(error) =>
    println(s"Parse error at line ${error.line}: ${error.message}")
}
```

**Output:**
```
Format: 4
Load steps: 2
Nodes: 1
Node: Root (Some(Node2D))
  position = Vector2(100.0, 200.0)
```

#### Example 2: Parse a .tres Resource File

```scala
val tresContent = """
[gd_resource type="Resource" format=4]

[resource]
name = "MyResource"
value = 42
active = true
"""

Parser.parseTres(tresContent) match {
  case Right(resource) =>
    println(s"Type: ${resource.resourceType}")
    println(s"Properties: ${resource.properties}")

  case Left(error) =>
    println(s"Error: ${error.message}")
}
```

#### Example 3: Parse Config File

```scala
val configContent = """
[application]
config/name = "MyGame"
run/main_scene = "res://main.tscn"

[display]
window/size/width = 1920
window/size/height = 1080
"""

Parser.parseConfig(configContent) match {
  case Right(config) =>
    config.sections.foreach { case (sectionName, properties) =>
      println(s"[$sectionName]")
      properties.foreach { case (key, value) =>
        println(s"  $key = $value")
      }
    }

  case Left(error) =>
    println(s"Error: ${error.message}")
}
```

### Lower-Level API: Parsing Individual Variants

If you need to parse just a variant value (not a full file), you can use `VariantParser` directly:

```scala
import io.github.optical002.godot.parser.parser.VariantParser
import io.github.optical002.godot.parser.tokenizer.{CharStream, VariantTokenizer}
import io.github.optical002.godot.parser.parser.ConstructParser
import io.github.optical002.godot.parser.core.Variant

// Create tokenizer
val content = "Vector2(10.5, 20)"
val stream = CharStream(content)
val tokenizer = VariantTokenizer(stream)

// Collect tokens
val tokens = {
  val builder = Vector.newBuilder[Token]
  var continue = true
  while (continue) {
    tokenizer.getToken() match {
      case Right(token) =>
        if (token.tokenType == TokenType.EOF) continue = false
        else builder += token
      case Left(err) => throw new RuntimeException(err.message)
    }
  }
  new TokenIterator(builder.result())
}

// Parse the value
VariantParser.parseValue(tokens) match {
  case Right(variant) =>
    println(variant) // Variant.Vector2(10.5, 20.0)

  case Left(error) =>
    println(s"Error: ${error.message}")
}
```

### Supported Variant Formats in Text

Here are examples of how each variant type is written in Godot text format:

```
# Primitives
null
true
false
42
3.14159
1.5e-3
"Hello World"
&"StringName"

# Math types
Vector2(10, 20)
Vector2i(10, 20)
Vector3(1, 2, 3)
Vector4(1, 2, 3, 4)
Rect2(0, 0, 100, 100)
Transform2D(1, 0, 0, 1, 0, 0)
Color(1.0, 0.5, 0.0, 1.0)
#ff0000         # Color from hex

# Resources
ExtResource("id_string")
SubResource("id_string")
NodePath("path/to/node")

# Collections
[1, 2, 3]                                    # Array
{key1: "value1", key2: 42}                   # Dictionary

# Packed arrays
PackedInt32Array(1, 2, 3, 4)
PackedFloat32Array(1.0, 2.0, 3.0)
PackedByteArray("base64encodedstring")       # BASE64 format
PackedVector2Array(1, 2, 3, 4, 5, 6)        # Flattened: (1,2), (3,4), (5,6)
PackedColorArray(1, 0, 0, 1, 0, 1, 0, 1)    # Flattened: (r,g,b,a) pairs
```

### Error Handling

All parsing functions return `Either[ParseError, T]`. The `ParseError` type provides:

```scala
sealed trait ParseError {
  def message: String        // Human-readable error message
  def line: Int             // Line number where error occurred
  def context: String       // Context around the error location
}

// Specific error types:
case class TokenizeError(...)  // Character-level errors
case class SyntaxError(...)    // Token-level errors
case class SemanticError(...)  // Structural errors
case class UnsupportedVersion(...) // Version compatibility
```

**Example Error Handling:**
```scala
Parser.parseTscn(content) match {
  case Right(scene) =>
    // Success
    processScene(scene)

  case Left(error) =>
    error match {
      case ParseError.SyntaxError(msg, line, context, expected, actual) =>
        println(s"Syntax error at line $line: $msg")
        println(s"Expected: ${expected.getOrElse("unknown")}")
        println(s"Got: ${actual.getOrElse("unknown")}")

      case ParseError.UnsupportedVersion(msg, version, line, context) =>
        println(s"Unsupported format version $version at line $line")

      case other =>
        println(s"Parse error: ${other.message}")
    }
}
```

---

## Summary

### Complete Flow

1. **Input:** Raw text string (e.g., contents of a `.tscn` file)
2. **Tokenizer:** Converts characters → tokens
3. **Parser:** Converts tokens → tags and variants
4. **Assembler:** Converts tags → strongly-typed structures
5. **Output:** `PackedScene`, `TextResource`, or `ConfigFile`

### Quick Start

```scala
// For .tscn files
Parser.parseTscn(tscnContent) match {
  case Right(scene) => // Use scene
  case Left(error) => // Handle error
}

// For .tres files
Parser.parseTres(tresContent) match {
  case Right(resource) => // Use resource
  case Left(error) => // Handle error
}

// For .godot/.cfg/.import files
Parser.parseConfig(configContent) match {
  case Right(config) => // Use config
  case Left(error) => // Handle error
}
```

### Key Points

- All 42 Godot variant types are supported
- Parser is 1-to-1 match with Godot's C++ implementation
- Three-layer architecture: Tokenizer → Parser → Assembler
- Functional error handling with `Either[ParseError, T]`
- No mutation, purely functional design
- Cross-platform: runs on JVM and Native (Scala Native)

---

## Godot Parser Implementation Details

### Complete Variant Type to Parser Location Mapping

This section maps each Variant type to its exact parser implementation in the Godot source code, including all possible parsing cases and compatibility aliases.

**Main Parser File:** `/home/optick/work/godot/core/variant/variant_parser.cpp`

---

### Tokenization (Lines 161-515)

Before variant values are parsed, the raw text is tokenized. Here are the token parsers:

#### Token: String (Lines 276-412)
**Format:** `"text with escapes"`
**Special cases:**
- Escape sequences: `\b` (backspace), `\t` (tab), `\n` (newline), `\f` (form feed), `\r` (carriage return)
- Unicode escapes: `\u####` (4 hex digits), `\U######` (6 hex digits)
- UTF-16 surrogate pairs: Validates and combines lead (0xd800-0xdbff) + trail (0xdc00-0xdfff) surrogates
- Any other escaped character: `\\`, `\"`, etc.

#### Token: StringName (Lines 265-275, 405-407)
**Formats:**
- `&"name"` (Godot 4.x)
- `@"name"` (Godot 3.x compatibility)
**Parser:** Same as String parser, but wraps result in StringName

#### Token: Color (Lines 241-260)
**Format:** `#RGB`, `#RGBA`, `#RRGGBB`, `#RRGGBBAA`
**Parser:** Reads hex digits after `#`, converts to Color using `Color::html()`

#### Token: Number (Lines 424-492)
**Formats:**
- Integer: `123`, `-456`
- Float: `1.5`, `3.14159`, `-2.5`
- Scientific notation: `1e10`, `1.5e-3`, `2E+5`
**States:** READING_INT → READING_DEC → READING_EXP
**Auto-detection:** Presence of `.` or `e`/`E` makes it a float

#### Token: Identifier (Lines 493-506)
**Format:** Starts with letter or `_`, followed by letters, digits, or `_`
**Used for:** Keywords (`true`, `false`, `null`), construct names (`Vector2`, `Color`), special values (`inf`, `nan`)

---

### Variant Value Parsing (Lines 676-1641)

Main entry point: `parse_value()` at line 676

#### 1. NIL (Lines 699-700)
**Location:** Lines 699-700
**Identifiers:** `"null"`, `"nil"`
**Result:** `Variant()` (empty variant)

#### 2. BOOL (Lines 695-698)
**Location:** Lines 695-698
**Identifiers:** `"true"`, `"false"`
**Result:** Boolean variant

#### 3. INT & FLOAT (Lines 1625-1627)
**Location:** Lines 1625-1627
**Token:** TK_NUMBER
**Result:** Integer or float based on token parsing

#### 4. STRING (Lines 1628-1630)
**Location:** Lines 1628-1630
**Token:** TK_STRING
**Result:** String variant

#### 5. STRING_NAME (Lines 1631-1633)
**Location:** Lines 1631-1633
**Token:** TK_STRING_NAME
**Result:** StringName variant

---

### Math Types - 2D

#### 6. VECTOR2 (Lines 708-720)
**Location:** Lines 708-720
**Format:** `Vector2(x, y)`
**Arguments:** 2 real_t (float/double)
**Parser:** `_parse_construct<real_t>`

#### 7. VECTOR2I (Lines 721-733)
**Location:** Lines 721-733
**Format:** `Vector2i(x, y)`
**Arguments:** 2 int32_t
**Parser:** `_parse_construct<int32_t>`

#### 8. RECT2 (Lines 734-746)
**Location:** Lines 734-746
**Format:** `Rect2(x, y, width, height)`
**Arguments:** 4 real_t
**Parser:** `_parse_construct<real_t>`

#### 9. RECT2I (Lines 747-759)
**Location:** Lines 747-759
**Format:** `Rect2i(x, y, width, height)`
**Arguments:** 4 int32_t
**Parser:** `_parse_construct<int32_t>`

---

### Math Types - 3D

#### 10. VECTOR3 (Lines 760-772)
**Location:** Lines 760-772
**Format:** `Vector3(x, y, z)`
**Arguments:** 3 real_t
**Parser:** `_parse_construct<real_t>`

#### 11. VECTOR3I (Lines 773-785)
**Location:** Lines 773-785
**Format:** `Vector3i(x, y, z)`
**Arguments:** 3 int32_t
**Parser:** `_parse_construct<int32_t>`

#### 12. PLANE (Lines 829-841)
**Location:** Lines 829-841
**Format:** `Plane(a, b, c, d)`
**Arguments:** 4 real_t (normal + distance)
**Parser:** `_parse_construct<real_t>`

---

### Math Types - 4D

#### 13. VECTOR4 (Lines 786-798)
**Location:** Lines 786-798
**Format:** `Vector4(x, y, z, w)`
**Arguments:** 4 real_t
**Parser:** `_parse_construct<real_t>`

#### 14. VECTOR4I (Lines 799-811)
**Location:** Lines 799-811
**Format:** `Vector4i(x, y, z, w)`
**Arguments:** 4 int32_t
**Parser:** `_parse_construct<int32_t>`

---

### Math Types - Transforms

#### 15. TRANSFORM2D (Lines 812-828)
**Location:** Lines 812-828
**Format:** `Transform2D(xx, xy, yx, yy, ox, oy)`
**Aliases:** `"Transform2D"`, `"Matrix32"` (compatibility)
**Arguments:** 6 real_t (2x2 matrix + origin)
**Parser:** `_parse_construct<real_t>`
**Note:** Constructs Transform2D with 3 Vector2: [0]=xx,xy; [1]=yx,yy; [2]=ox,oy

#### 16. QUATERNION (Lines 842-854)
**Location:** Lines 842-854
**Format:** `Quaternion(x, y, z, w)`
**Aliases:** `"Quaternion"`, `"Quat"` (compatibility)
**Arguments:** 4 real_t
**Parser:** `_parse_construct<real_t>`

#### 17. AABB (Lines 855-867)
**Location:** Lines 855-867
**Format:** `AABB(px, py, pz, sx, sy, sz)`
**Aliases:** `"AABB"`, `"Rect3"` (compatibility)
**Arguments:** 6 real_t (position + size)
**Parser:** `_parse_construct<real_t>`
**Note:** Constructs AABB(Vector3(px,py,pz), Vector3(sx,sy,sz))

#### 18. BASIS (Lines 868-880)
**Location:** Lines 868-880
**Format:** `Basis(xx, xy, xz, yx, yy, yz, zx, zy, zz)`
**Aliases:** `"Basis"`, `"Matrix3"` (compatibility)
**Arguments:** 9 real_t (3x3 matrix)
**Parser:** `_parse_construct<real_t>`

#### 19. TRANSFORM3D (Lines 881-893)
**Location:** Lines 881-893
**Format:** `Transform3D(bxx, bxy, bxz, byx, byy, byz, bzx, bzy, bzz, ox, oy, oz)`
**Aliases:** `"Transform3D"`, `"Transform"` (Godot <4 compatibility)
**Arguments:** 12 real_t (3x3 basis + origin)
**Parser:** `_parse_construct<real_t>`
**Note:** Constructs Transform3D(Basis(9 args), Vector3(3 args))

#### 20. PROJECTION (Lines 894-906)
**Location:** Lines 894-906
**Format:** `Projection(xx, xy, xz, xw, yx, yy, yz, yw, zx, zy, zz, zw, wx, wy, wz, ww)`
**Arguments:** 16 real_t (4x4 matrix)
**Parser:** `_parse_construct<real_t>`
**Note:** Constructs Projection(Vector4(4), Vector4(4), Vector4(4), Vector4(4))

---

### Misc Types

#### 21. COLOR (Lines 907-919, 1634-1636)
**Location:**
- Construct: Lines 907-919
- Token: Lines 1634-1636
**Formats:**
- Construct: `Color(r, g, b, a)` - Arguments: 4 float
- Token: `#RGB`, `#RGBA`, `#RRGGBB`, `#RRGGBBAA`
**Parser:**
- Construct: `_parse_construct<float>`
- Token: Direct from TK_COLOR token

#### 22. NODE_PATH (Lines 920-939)
**Location:** Lines 920-939
**Format:** `NodePath("path/to/node")`
**Arguments:** 1 string
**Special:** Expects TK_STRING token after opening parenthesis

#### 23. RID (Lines 940-963)
**Location:** Lines 940-963
**Format:** `RID(number)` or `RID()` (empty)
**Arguments:** 0 or 1 number (uint64)
**Special cases:**
- Empty RID: `RID()` → creates null RID
- With ID: `RID(12345)` → creates RID from uint64

#### 24. OBJECT (Lines 994-1088)
**Location:** Lines 994-1088
**Format:** `Object(TypeName, "property1": value1, "property2": value2, ...)`
**Parser:** Custom parser
**Process:**
1. Expects identifier with type name after `(`
2. Instantiates object using ClassDB
3. Parses comma-separated property assignments
4. Properties are `"key": value` pairs
5. Returns RefCounted reference if applicable, otherwise raw Object

#### 25. CALLABLE (Lines 979-993)
**Location:** Lines 979-993
**Format:** `Callable()`
**Arguments:** None (always empty in text format)
**Note:** Text format only supports empty Callables

#### 26. SIGNAL (Lines 964-978)
**Location:** Lines 964-978
**Format:** `Signal()`
**Arguments:** None (always empty in text format)
**Note:** Text format only supports empty Signals

---

### Resource References

#### Resource/SubResource/ExtResource (Lines 1089-1186)
**Location:** Lines 1089-1186
**Formats:**
- `Resource("path")` or `Resource("uid://...", "path")`
- `SubResource("id")` or `SubResource(id)`
- `ExtResource("id")` or `ExtResource(id)`

**Special Handling:**
- If `p_res_parser` is provided, uses custom parsers for each type
- Otherwise, parses as string argument(s)
- Resource can have:
  - Just path: `Resource("res://path")`
  - Just UID: `Resource("uid://...")`
  - Both: `Resource("uid://...", "res://path")`
- UID takes priority if both provided

---

### Collections

#### 27. DICTIONARY (Lines 677-684, 1187-1326)
**Location:**
- Untyped: Lines 677-684
- Typed: Lines 1187-1326
**Formats:**
- Untyped: `{key1: value1, key2: value2, ...}`
- Typed: `Dictionary[KeyType, ValueType]({key1: value1, ...})`
**Token:** TK_CURLY_BRACKET_OPEN for untyped
**Typed Format:** `Dictionary[type1, type2]({...})`
- Supports builtin types, class names, and Resource scripts
- Parser: `_parse_dictionary()`

#### 28. ARRAY (Lines 685-692, 1327-1409)
**Location:**
- Untyped: Lines 685-692
- Typed: Lines 1327-1409
**Formats:**
- Untyped: `[element1, element2, ...]`
- Typed: `Array[ElementType]([element1, element2, ...])`
**Token:** TK_BRACKET_OPEN for untyped
**Typed Format:** `Array[type]([...])`
- Supports builtin types, class names, and Resource scripts
- Parser: `_parse_array()`

---

### Packed Arrays

#### 29. PACKED_BYTE_ARRAY (Lines 1410-1427)
**Location:** Lines 1410-1427
**Formats:**
- BASE64: `PackedByteArray("base64string")`
- Numbers: `PackedByteArray(1, 2, 3, ...)`
**Aliases:** `"PackedByteArray"`, `"PoolByteArray"` (Godot 3.x), `"ByteArray"`
**Parser:** `_parse_byte_array()` (lines 600-674)
**Special:**
- If first token is TK_STRING → decode as BASE64
- If first token is TK_NUMBER → parse comma-separated integers
- Empty array: `PackedByteArray()`

#### 30. PACKED_INT32_ARRAY (Lines 1428-1445)
**Location:** Lines 1428-1445
**Format:** `PackedInt32Array(1, 2, 3, ...)`
**Aliases:** `"PackedInt32Array"`, `"PackedIntArray"`, `"PoolIntArray"` (Godot 3.x), `"IntArray"`
**Parser:** `_parse_construct<int32_t>`

#### 31. PACKED_INT64_ARRAY (Lines 1446-1463)
**Location:** Lines 1446-1463
**Format:** `PackedInt64Array(1, 2, 3, ...)`
**Parser:** `_parse_construct<int64_t>`

#### 32. PACKED_FLOAT32_ARRAY (Lines 1464-1481)
**Location:** Lines 1464-1481
**Format:** `PackedFloat32Array(1.0, 2.0, 3.0, ...)`
**Aliases:** `"PackedFloat32Array"`, `"PackedRealArray"`, `"PoolRealArray"` (Godot 3.x), `"FloatArray"`
**Parser:** `_parse_construct<float>`

#### 33. PACKED_FLOAT64_ARRAY (Lines 1482-1499)
**Location:** Lines 1482-1499
**Format:** `PackedFloat64Array(1.0, 2.0, 3.0, ...)`
**Parser:** `_parse_construct<double>`

#### 34. PACKED_STRING_ARRAY (Lines 1500-1545)
**Location:** Lines 1500-1545
**Format:** `PackedStringArray("str1", "str2", "str3", ...)`
**Aliases:** `"PackedStringArray"`, `"PoolStringArray"` (Godot 3.x), `"StringArray"`
**Parser:** Custom loop parser
**Special:** Expects TK_STRING tokens separated by commas

#### 35. PACKED_VECTOR2_ARRAY (Lines 1546-1563)
**Location:** Lines 1546-1563
**Format:** `PackedVector2Array(x1, y1, x2, y2, x3, y3, ...)`
**Aliases:** `"PackedVector2Array"`, `"PoolVector2Array"` (Godot 3.x), `"Vector2Array"`
**Parser:** `_parse_construct<real_t>`
**Special:** Values are flattened - pairs of numbers become Vector2
**Note:** args.size() / 2 = number of Vector2s

#### 36. PACKED_VECTOR3_ARRAY (Lines 1564-1581)
**Location:** Lines 1564-1581
**Format:** `PackedVector3Array(x1, y1, z1, x2, y2, z2, ...)`
**Aliases:** `"PackedVector3Array"`, `"PoolVector3Array"` (Godot 3.x), `"Vector3Array"`
**Parser:** `_parse_construct<real_t>`
**Special:** Values are flattened - triplets of numbers become Vector3
**Note:** args.size() / 3 = number of Vector3s

#### 37. PACKED_COLOR_ARRAY (Lines 1600-1617)
**Location:** Lines 1600-1617
**Format:** `PackedColorArray(r1, g1, b1, a1, r2, g2, b2, a2, ...)`
**Aliases:** `"PackedColorArray"`, `"PoolColorArray"` (Godot 3.x), `"ColorArray"`
**Parser:** `_parse_construct<float>`
**Special:** Values are flattened - quads of floats become Color(r,g,b,a)
**Note:** args.size() / 4 = number of Colors

#### 38. PACKED_VECTOR4_ARRAY (Lines 1582-1599)
**Location:** Lines 1582-1599
**Format:** `PackedVector4Array(x1, y1, z1, w1, x2, y2, z2, w2, ...)`
**Aliases:** `"PackedVector4Array"`, `"PoolVector4Array"` (Godot 3.x), `"Vector4Array"`
**Parser:** `_parse_construct<real_t>`
**Special:** Values are flattened - quads of numbers become Vector4
**Note:** args.size() / 4 = number of Vector4s

---

### Special Float Values

#### inf (Lines 701-702)
**Location:** Lines 701-702
**Identifier:** `"inf"`
**Result:** `Math::INF` (positive infinity)

#### -inf (Lines 703-705)
**Location:** Lines 703-705
**Identifiers:** `"-inf"`, `"inf_neg"` (compatibility)
**Result:** `-Math::INF` (negative infinity)

#### nan (Lines 706-707)
**Location:** Lines 706-707
**Identifier:** `"nan"`
**Result:** `Math::NaN` (not a number)

---

## Parsing Patterns Summary

### Construct Parsing Pattern

All constructs follow this pattern:
```
ConstructName(arg1, arg2, ..., argN)
```

**Generic Parse Flow:**
1. Identifier token detected (`Vector2`, `Color`, etc.)
2. Check for `(` token
3. Parse comma-separated arguments using type-specific parser:
   - `_parse_construct<T>` for numeric types
   - `_parse_byte_array` for PackedByteArray (supports BASE64)
   - Custom loops for PackedStringArray
4. Check for `)` token
5. Construct variant from arguments

### Flattened Array Pattern

Some packed arrays use flattened values:
- **PackedVector2Array:** `(x1, y1, x2, y2)` → `[Vector2(x1,y1), Vector2(x2,y2)]`
- **PackedVector3Array:** `(x1, y1, z1, x2, y2, z2)` → `[Vector3(x1,y1,z1), Vector3(x2,y2,z2)]`
- **PackedColorArray:** `(r1, g1, b1, a1, r2, g2, b2, a2)` → `[Color(r1,g1,b1,a1), Color(r2,g2,b2,a2)]`
- **PackedVector4Array:** `(x1, y1, z1, w1, x2, y2, z2, w2)` → `[Vector4(x1,y1,z1,w1), Vector4(x2,y2,z2,w2)]`

### Typed Collection Pattern

Typed arrays and dictionaries:
```
Array[ElementType]([elements])
Dictionary[KeyType, ValueType]({key: value pairs})
```

**Type specification:**
- Builtin types: `Int`, `String`, `Vector2`, etc.
- Object types: Class names from ClassDB
- Script types: `Resource("path/to/script.gd")`

### Helper Parsers

1. **`_parse_construct<T>`** (lines 552-598)
   - Generic numeric construct parser
   - Supports: int32_t, int64_t, float, double, real_t
   - Handles: `(num1, num2, ..., numN)` or `()`

2. **`_parse_byte_array`** (lines 600-674)
   - Special parser for PackedByteArray
   - Detects BASE64 string vs. numeric array
   - BASE64: `("string")` → decodes using CryptoCore::b64_decode
   - Numeric: `(1, 2, 3)` → array of bytes

3. **`_parse_array`** (lines 1643-1682)
   - Recursive array parser
   - Handles nested arrays and any variant values
   - Comma-separated with optional trailing comma

4. **`_parse_dictionary`** (not shown, but referenced)
   - Recursive dictionary parser
   - Format: `key1: value1, key2: value2`
   - Both keys and values can be any variant type

---

## Compatibility Aliases

For backwards compatibility with Godot 3.x:

| Current Name | 3.x Alias |
|--------------|-----------|
| Transform2D | Matrix32 |
| Transform3D | Transform |
| Basis | Matrix3 |
| Quaternion | Quat |
| AABB | Rect3 |
| PackedByteArray | PoolByteArray, ByteArray |
| PackedInt32Array | PackedIntArray, PoolIntArray, IntArray |
| PackedFloat32Array | PackedRealArray, PoolRealArray, FloatArray |
| PackedStringArray | PoolStringArray, StringArray |
| PackedVector2Array | PoolVector2Array, Vector2Array |
| PackedVector3Array | PoolVector3Array, Vector3Array |
| PackedColorArray | PoolColorArray, ColorArray |
| PackedVector4Array | PoolVector4Array, Vector4Array |
| StringName (`&"..."`) | `@"..."` |

---

## Error Handling Cases

The parser handles these error cases:

### String Parsing Errors
- Unterminated string (missing closing `"`)
- Invalid escape sequence
- Malformed hex constant in `\u` or `\U`
- Invalid UTF-16 surrogate pairs:
  - Unpaired lead surrogate (0xd800-0xdbff without trail)
  - Unpaired trail surrogate (0xdc00-0xdfff without lead)
  - Wrong order (trail before lead)

### Construct Parsing Errors
- Missing `(` after construct name
- Wrong number of arguments
- Wrong type of arguments
- Missing `)` after arguments
- Missing `,` between arguments

### Resource Parsing Errors
- Can't instantiate Object type
- Can't load resource at path
- Invalid resource reference format

### Collection Parsing Errors
- Unterminated array/dictionary (EOF)
- Missing `,` between elements
- Missing `:` in dictionary key-value pairs
- Type mismatch in typed collections

### Special Value Errors
- `RID()` with non-numeric argument
- BASE64 decoding failure in PackedByteArray
- Invalid identifier (not recognized as keyword or construct)

---

## Usage Notes for Scala Implementation

When implementing the Scala parser, pay attention to:

1. **Compatibility aliases** - Support both current and legacy names
2. **Flattened arrays** - Vector/Color arrays use flat number lists
3. **BASE64 detection** - PackedByteArray must check if first arg is string
4. **Empty constructs** - All constructs support `()` for empty/default
5. **UTF-16 surrogates** - Proper validation required for `\u` escapes
6. **Scientific notation** - Numbers can have `e` or `E` notation
7. **Special floats** - Support `inf`, `-inf`, `inf_neg`, `nan`
8. **Typed collections** - Complex type specification syntax
9. **Resource references** - Can be ExtResource, SubResource, or Resource with path/UID
