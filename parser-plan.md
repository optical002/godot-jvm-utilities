# Godot Text Format Parser Implementation Plan

## Overview

Implement a 1-to-1 replication of Godot's text format parser in Scala 3, supporting `.tscn`, `.tres`, `.godot/.cfg`, and `.import` files with full structural parsing for validation and code generation.

**Reference Implementation:** `/home/optick/work/godot/core/variant/variant_parser.cpp` and `/home/optick/work/godot/scene/resources/resource_format_text.cpp`

## Architecture: Three-Layer Design

```
Input String → Tokenizer → Parser → Assembler → Parsed Output
               (Lexical)   (Syntactic) (Semantic)
```

### Layer 1: Tokenizer
- **Input:** Raw string content
- **Output:** Stream of tokens (identifiers, numbers, strings, operators, brackets)
- **Files:** `tokenizer/VariantTokenizer.scala`, `tokenizer/CharStream.scala`
- **Key Functions:** Character reading, escape sequences, number parsing, color parsing

### Layer 2: Parser
- **Input:** Token stream
- **Output:** Tags (sections) + Variants (typed values)
- **Files:** `parser/VariantParser.scala`, `parser/ConstructParser.scala`
- **Key Functions:** `parseTag()`, `parseValue()`, `parseConstruct()`

### Layer 3: Assembler
- **Input:** Tags + Variants
- **Output:** Strongly-typed structures (PackedScene, TextResource, ConfigFile)
- **Files:** `assembler/SceneAssembler.scala`, `assembler/ResourceAssembler.scala`, `assembler/ConfigAssembler.scala`

## Module Structure

```
godotParser/src/main/scala/io/github/optical002/godot/parser/
├── Parser.scala                    (Public API entry point)
├── core/
│   ├── Variant.scala              (40+ Godot types as enum)
│   ├── Token.scala                (Token types and Token case class)
│   ├── Tag.scala                  (Section tags with fields)
│   └── ParseError.scala           (Error handling ADT)
├── tokenizer/
│   ├── CharStream.scala           (String-based stream with line tracking)
│   └── VariantTokenizer.scala    (Lexical analysis - chars → tokens)
├── parser/
│   ├── VariantParser.scala       (Syntactic analysis - tokens → tags/variants)
│   └── ConstructParser.scala     (Special constructs: Vector2(), Color(), etc.)
├── assembler/
│   ├── SceneAssembler.scala      (.tscn → PackedScene)
│   ├── ResourceAssembler.scala   (.tres → TextResource)
│   └── ConfigAssembler.scala     (.godot/.cfg → ConfigFile)
└── model/
    ├── ParsedTypes.scala         (PackedScene, TextResource, ConfigFile)
    ├── ResourceTypes.scala       (ExtResource, SubResource)
    └── NodeTypes.scala           (NodeData, ConnectionData)
```

## Core Data Structures

### Error Handling
```scala
sealed trait ParseError {
  def message: String
  def line: Int
  def context: String
}

type ParseResult[T] = Either[ParseError, T]
```

**Error Types:** `TokenizeError`, `SyntaxError`, `SemanticError`, `UnsupportedVersion`

### Token System
```scala
enum TokenType:
  case CurlyBracketOpen, CurlyBracketClose    // { }
  case BracketOpen, BracketClose               // [ ]
  case ParenthesisOpen, ParenthesisClose       // ( )
  case Identifier, String, StringName          // abc, "text", &"name"
  case Number, Color                            // 123, #ff00ff
  case Colon, Comma, Period, Equal             // : , . =
  case EOF, Error

case class Token(tokenType: TokenType, value: Variant, line: Int, column: Int)
```

### Tag Structure
```scala
case class Tag(
  name: String,                    // e.g., "gd_scene", "node", "ext_resource"
  fields: Map[String, Variant],   // Tag attributes
  line: Int
)
```

### Variant Type System (40+ Types)

**Primitives:**
- `Nil`, `Bool`, `Int`, `Float`, `String`, `StringName`

**Math Types:**
- `Vector2`, `Vector2i`, `Vector3`, `Vector3i`, `Vector4`, `Vector4i`
- `Rect2`, `Rect2i`, `AABB`, `Plane`
- `Transform2D`, `Transform3D`, `Basis`, `Quaternion`, `Projection`

**Special:**
- `Color`, `NodePath`, `RID`, `Callable`, `Signal`

**Collections:**
- `Array`, `Dictionary` (both support typed variants)
- `PackedByteArray`, `PackedInt32Array`, `PackedFloat32Array`, etc. (10 packed array types)

**Objects:**
- `Object(ExtResource(id))`, `Object(SubResource(id))`, `Object(Null)`

### Output Models

```scala
// .tscn files
case class PackedScene(
  loadSteps: Int,
  format: Int,
  uid: Option[String],
  extResources: Vector[ExtResource],
  subResources: Vector[SubResource],
  nodes: Vector[NodeData],
  connections: Vector[ConnectionData],
  editableInstances: Vector[String]
) extends Parsed

// .tres files
case class TextResource(
  resourceType: String,
  format: Int,
  uid: Option[String],
  extResources: Vector[ExtResource],
  subResources: Vector[SubResource],
  properties: Map[String, Variant]
) extends Parsed

// .godot, .cfg, .import files
case class ConfigFile(
  sections: Map[String, Map[String, Variant]]
) extends Parsed
```

## Parsing Flow

### For .tscn/.tres Files (Three-Phase)

**Phase 1: Parse Header + External Resources**
```
[gd_scene load_steps=2 format=4 uid="uid://..."]
[ext_resource type="Script" path="res://main.gd" id="1"]
```
- Extract metadata: format version, load steps, UID
- Build `extResources` map with IDs

**Phase 2: Parse Sub-Resources**
```
[sub_resource type="AnimationLibrary" id="1"]
property1 = value1
property2 = value2
```
- Create resource instances
- Parse properties as Variants
- Store in `subResources` map

**Phase 3: Parse Main Content**
- **For .tscn:** Parse `[node]` tags → build node hierarchy, parse `[connection]` tags → signal connections
- **For .tres:** Parse `[resource]` tag → extract all properties

### For .godot/.cfg/.import Files (Simple)

```
[section_name]
key1 = value1
key2 = value2
```
- Alternating pattern: tag → properties → tag → properties
- Build nested Map structure

## Implementation Phases

### Phase 1: Core Infrastructure ✓
**Goal:** Foundation - error handling, tokens, streams

**Files to create:**
1. `core/ParseError.scala` - Error ADT with rich context
2. `core/Token.scala` - Token types and Token case class
3. `core/Tag.scala` - Tag structure with fields map
4. `tokenizer/CharStream.scala` - Character stream with line/column tracking

**Key Features:**
- Line/column tracking in CharStream
- Context extraction for error messages (50 chars before/after)
- `ParseResult[T] = Either[ParseError, T]`

### Phase 2: Tokenizer ✓
**Goal:** Lexical analysis - strings → tokens

**Files to create:**
1. `tokenizer/VariantTokenizer.scala`

**Key Methods:**
- `getToken(): ParseResult[Token]` - Main tokenization
- `parseString()` - String parsing with escape sequences (UTF-16 surrogates)
- `parseNumber()` - Numbers with scientific notation, inf, nan
- `parseColor()` - Hex colors (#RGB, #RGBA, #RRGGBB, #RRGGBBAA)
- `parseIdentifier()` - Alphanumeric identifiers
- `skipWhitespaceAndComments()` - Ignore ; comments and whitespace

**Special Handling:**
- Escape sequences: `\n`, `\t`, `\r`, `\b`, `\f`, `\\`, `\"`, `\u####`, `\U######`
- UTF-16 surrogate pair validation
- StringName syntax: `&"name"` or `@"name"` (3.x compat)

### Phase 3: Variant Type System
**Goal:** Complete type system matching Godot's 40+ types

**Files to create:**
1. `core/Variant.scala`

**Implementation Details:**
- Single enum with 40+ cases
- Nested enums: `ObjectValue`, `ArrayType`, `DictionaryType`
- Extension methods for type extraction: `.asString`, `.asInt`, `.asVector2`, etc.
- Equality and hashing for Dictionary keys

### Phase 4: Variant Parser
**Goal:** Syntactic analysis - tokens → variants/tags

**Files to create:**
1. `parser/VariantParser.scala` - Main parsing logic
2. `parser/ConstructParser.scala` - Special construct parsing

**Key Methods:**

**VariantParser:**
- `parseTag(): ParseResult[Tag]` - Parse `[tag_name field1=value1 field2=value2]`
- `parseTagAssignEof(): ParseResult[(Tag, String, Variant)]` - Parse tag or `key = value`
- `parseValue(): ParseResult[Variant]` - Parse any variant value
- `parseArray(): ParseResult[Variant.Array]` - Parse `[1, 2, 3]`
- `parseDictionary(): ParseResult[Variant.Dictionary]` - Parse `{key1: val1, key2: val2}`

**ConstructParser:**
- `parseConstruct(name: String): ParseResult[Variant]` - Parse special constructs
- Handle: `Vector2()`, `Vector3()`, `Color()`, `NodePath()`, `ExtResource()`, `SubResource()`, etc.
- Parse typed arrays: `Array[int]([1, 2, 3])`
- Parse typed dictionaries: `Dictionary[String, int]({...})`
- Parse packed arrays: `PackedByteArray([1, 2, 3])`

**Parsing Rules:**
- Identifiers followed by `(` → construct
- `[` → array or tag (context-dependent)
- `{` → dictionary
- Quoted strings → String/StringName
- Numbers → Int/Float (auto-detect)
- `#` → Color
- `true`/`false` → Bool
- `null` → Nil

### Phase 5: Assemblers
**Goal:** Semantic analysis - tags → typed structures

**Files to create:**
1. `assembler/SceneAssembler.scala` - .tscn parsing
2. `assembler/ResourceAssembler.scala` - .tres parsing
3. `assembler/ConfigAssembler.scala` - .godot/.cfg/.import parsing

**SceneAssembler (.tscn):**
```scala
def assemble(tags: Vector[Tag]): ParseResult[PackedScene]
```
1. Extract header: `[gd_scene ...]` → format, loadSteps, uid
2. Parse `[ext_resource ...]` tags → build ExtResource objects
3. Parse `[sub_resource ...]` tags → parse properties → build SubResource objects
4. Parse `[node ...]` tags → build NodeData hierarchy
5. Parse `[connection ...]` tags → build ConnectionData
6. Parse `[editable ...]` tags → extract paths

**ResourceAssembler (.tres):**
```scala
def assemble(tags: Vector[Tag]): ParseResult[TextResource]
```
1. Extract header: `[gd_resource type="..." ...]` → resourceType, format, uid
2. Parse `[ext_resource ...]` tags
3. Parse `[sub_resource ...]` tags
4. Parse `[resource]` tag followed by properties → extract all key-value pairs

**ConfigAssembler (.godot/.cfg/.import):**
```scala
def assemble(tags: Vector[Tag]): ParseResult[ConfigFile]
```
- Simple: each `[section]` tag followed by key=value pairs
- Build nested Map: `Map[String, Map[String, Variant]]`

### Phase 6: Public API & Integration
**Goal:** Clean public API in Parser.scala

**File to modify:**
1. `/home/optick/work/godot-jvm-utilities/godotParser/src/main/scala/io/github/optical002/godot/parser/Parser.scala`

**Implementation:**
```scala
object Parser {
  enum Kind {
    case TSCN, TRES, Config
  }

  sealed trait Parsed

  def parse(content: String, kind: Kind): ParseResult[Parsed] = {
    val stream = CharStream(content)
    val tokenizer = VariantTokenizer(stream)

    // Collect all tokens first for error recovery
    val tokens = collectTokens(tokenizer)

    // Parse tags and variants
    val parser = VariantParser(tokens)
    val tags = parser.parseTags()

    // Assemble into typed structure
    kind match {
      case Kind.TSCN => SceneAssembler.assemble(tags)
      case Kind.TRES => ResourceAssembler.assemble(tags)
      case Kind.Config => ConfigAssembler.assemble(tags)
    }
  }
}
```

### Phase 7: Testing & Validation
**Goal:** Comprehensive test coverage

**Test Structure:**
```
godotParser/src/test/scala/
├── tokenizer/
│   ├── VariantTokenizerSpec.scala
│   └── CharStreamSpec.scala
├── parser/
│   ├── VariantParserSpec.scala
│   └── ConstructParserSpec.scala
├── assembler/
│   ├── SceneAssemblerSpec.scala
│   ├── ResourceAssemblerSpec.scala
│   └── ConfigAssemblerSpec.scala
└── integration/
    ├── RealWorldFilesSpec.scala
    └── RoundTripSpec.scala
```

**Test Data Sources:**
1. Create minimal test files for each construct
2. Use real Godot files from `/home/optick/work/angel-simulator/` for integration tests
3. Property-based testing with ScalaCheck for tokenizer/parser
4. Edge cases: empty files, malformed input, unsupported versions

**Validation:**
- Parse all .tscn/.tres files in angel-simulator
- Compare parsed structure against known valid files
- Ensure error messages are helpful (line numbers, context)

## Critical Implementation Details

### Character Stream Features
- UTF-8 input handling
- Line/column tracking (1-indexed)
- Single character lookahead via `saveChar()`
- Context extraction for error messages

### String Parsing Edge Cases
- Escape sequences: `\n`, `\t`, `\r`, `\b`, `\f`, `\\`, `\"`
- Unicode escapes: `\u####` (4 hex digits), `\U######` (6 hex digits)
- UTF-16 surrogate pairs: validate lead (0xd800-0xdbff) + trail (0xdc00-0xdfff)
- Unterminated strings → helpful error

### Number Parsing
- Integer: `123`, `-456`
- Float: `1.5`, `3.14159`, `-2.5`
- Scientific: `1e10`, `1.5e-3`, `2E+5`
- Special: `inf`, `-inf`, `inf_neg`, `nan`

### Color Parsing
- 3-digit: `#RGB` → expand to `#RRGGBB`
- 4-digit: `#RGBA` → expand to `#RRGGBBAA`
- 6-digit: `#RRGGBB`
- 8-digit: `#RRGGBBAA`
- Convert hex to 0.0-1.0 float range

### Tag Parsing Rules
From Godot's format:
- `[tag_name]` - Tag with no fields
- `[tag_name field1=value1 field2=value2]` - Tag with fields
- Tag terminates at `]`
- Fields are space-separated `key=value` pairs
- Values can be any Variant type

### Resource References
- `ExtResource("id")` or `ExtResource(id)` → resolve to external file
- `SubResource("id")` or `SubResource(id)` → resolve to internal resource
- IDs can be strings or integers (both supported)

## Version Compatibility

**Supported Formats:**
- Format 3 (Godot 3.x) - limited compatibility
- Format 4 (Godot 4.x) - full support

**Version-Specific Handling:**
- Format 4: PackedVector4Array, base64 PackedByteArray
- Format 3: Different string IDs, no Vector4
- Check `format` field in header tag
- Error on unsupported versions > 4

## Error Handling Strategy

**Rich Error Context:**
```scala
ParseError.SyntaxError(
  message = "Expected ']' to close tag",
  line = 15,
  context = """
    [node name="Player" type="CharacterBody2D"
                                              ^
  """,
  expected = Some("]"),
  actual = Some("EOF")
)
```

**Error Recovery:**
- No recovery in initial implementation
- Fail fast with detailed error
- Line/column information for all errors
- Context window showing 50 chars before/after error

## Testing Strategy

### Unit Tests
- Each component isolated
- Mock dependencies
- Edge case coverage (empty, null, malformed)

### Integration Tests
- Real Godot files from angel-simulator
- Parse → verify structure matches expectations
- Test all file types: .tscn, .tres, .godot, .import

### Property-Based Tests (ScalaCheck)
- Generate random valid inputs
- Verify tokenizer never crashes
- Verify parser produces valid structures

### Regression Suite
- Collect problematic files that fail
- Add to regression suite
- Ensure fixes don't break existing functionality

## Files to Modify

**Primary:**
- `/home/optick/work/godot-jvm-utilities/godotParser/src/main/scala/io/github/optical002/godot/parser/Parser.scala`

**To Create (25 files):**
1. `core/ParseError.scala`
2. `core/Token.scala`
3. `core/Tag.scala`
4. `core/Variant.scala`
5. `tokenizer/CharStream.scala`
6. `tokenizer/VariantTokenizer.scala`
7. `parser/VariantParser.scala`
8. `parser/ConstructParser.scala`
9. `assembler/SceneAssembler.scala`
10. `assembler/ResourceAssembler.scala`
11. `assembler/ConfigAssembler.scala`
12. `model/ParsedTypes.scala`
13. `model/ResourceTypes.scala`
14. `model/NodeTypes.scala`
15-25. Test files (11 test specs)

## Build Configuration

**Already correct in build.sbt:**
```scala
lazy val godotParser = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)  // ✓ Same code for both platforms
  .in(file("godotParser"))
  .settings(
    name := "godot-parser",
    version := "0.1.0",
    scalaVersion := "3.7.4"
  )
```

**Add test dependencies:**
```scala
libraryDependencies ++= Seq(
  "org.scalameta" %%% "munit" % "1.0.0" % Test,
  "org.scalacheck" %%% "scalacheck" % "1.18.0" % Test
)
```

## Success Criteria

1. ✓ Parse all .tscn files in angel-simulator without errors
2. ✓ Parse all .tres files in angel-simulator without errors
3. ✓ Parse project.godot and .import files correctly
4. ✓ Extract all node properties, connections, resources accurately
5. ✓ Match Godot's internal representation 1-to-1
6. ✓ Helpful error messages with line numbers and context
7. ✓ 90%+ test coverage
8. ✓ Compiles and runs on both JVM and Native

## Next Steps After Implementation

1. Integrate parser into angel-simulator validator
2. Replace existing TresParser/TscnParser implementations
3. Use parsed structures for validation rules
4. Use parsed structures for code generation
5. Consider: write-back support (modify and save files)
