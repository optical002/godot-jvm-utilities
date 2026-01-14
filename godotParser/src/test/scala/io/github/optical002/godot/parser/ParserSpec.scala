package io.github.optical002.godot.parser

import io.github.optical002.godot.parser.core.ParseError
import io.github.optical002.godot.parser.model.{ConfigFile, PackedScene, TextResource}

class ParserSpec extends munit.FunSuite {

  test("parse simple .tscn file") {
    val tscn = """[gd_scene load_steps=2 format=4]
[node name="Root" type="Node2D"]
"""

    Parser.parse(tscn, Parser.Kind.TSCN) match {
      case Right(parsed: PackedScene) =>
        assertEquals(parsed.loadSteps, 2)
        assertEquals(parsed.format, 4)
        assertEquals(parsed.nodes.length, 1)
        assertEquals(parsed.nodes.head.name, "Root")
        assertEquals(parsed.nodes.head.nodeType, Some("Node2D"))

      case Right(other) =>
        fail(s"Expected PackedScene, got ${other.getClass.getSimpleName}")

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("parse simple .tres file") {
    val tres = """[gd_resource type="Resource" format=4]
[resource]
name = "TestResource"
value = 42
"""

    Parser.parse(tres, Parser.Kind.TRES) match {
      case Right(parsed: TextResource) =>
        assertEquals(parsed.resourceType, "Resource")
        assertEquals(parsed.format, 4)
        assertEquals(parsed.properties.size, 2)

      case Right(other) =>
        fail(s"Expected TextResource, got ${other.getClass.getSimpleName}")

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("parse simple config file") {
    val config = """[section1]
key1 = "value1"
key2 = 123

[section2]
key3 = true
"""

    Parser.parse(config, Parser.Kind.Config) match {
      case Right(parsed: ConfigFile) =>
        assertEquals(parsed.sections.size, 2)
        assert(parsed.sections.contains("section1"))
        assert(parsed.sections.contains("section2"))

      case Right(other) =>
        fail(s"Expected ConfigFile, got ${other.getClass.getSimpleName}")

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("parse Vector2 construct") {
    val tscn = """[gd_scene format=4]
[node name="Test" type="Node"]
position = Vector2(10.5, 20.5)
"""

    Parser.parse(tscn, Parser.Kind.TSCN) match {
      case Right(parsed: PackedScene) =>
        val node = parsed.nodes.head
        assert(node.properties.contains("position"))

      case Right(other) =>
        fail(s"Expected PackedScene, got ${other.getClass.getSimpleName}")

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("parse Color from hex") {
    val tscn = """[gd_scene format=4]
[node name="Test" type="Node"]
color = #ff0000
"""

    Parser.parse(tscn, Parser.Kind.TSCN) match {
      case Right(parsed: PackedScene) =>
        val node = parsed.nodes.head
        assert(node.properties.contains("color"))

      case Right(other) =>
        fail(s"Expected PackedScene, got ${other.getClass.getSimpleName}")

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("Complicated .tscn file") {
    // TODO The parser does not try to parse BASE64 as string, only raw bytes fix parser.
    // TODO Write tests for all Construct Parsers
    val tscn =
      """
        |[gd_scene load_steps=4 format=4 uid="uid://cvyw4gqmo3707"]
        |
        |[ext_resource type="TileSet" uid="uid://bm6mpf2tuta5k" path="res://game/resources/tilesets/floor_tile_set.tres" id="2_48f77"]
        |[ext_resource type="TileSet" uid="uid://c21r3jbjdl1oc" path="res://game/resources/tilesets/windows_stairs_doors_tile_set.tres" id="3_ms3in"]
        |[ext_resource type="PackedScene" uid="uid://bmemrydpwj1o3" path="res://game/prefabs/characters/charater_base.tscn" id="4_fu6cl"]
        |
        |[node name="Game" type="Node2D"]
        |y_sort_enabled = true
        |
        |[node name="Floor" type="TileMapLayer" parent="."]
        |z_index = -10
        |tile_map_data = PackedByteArray("AAD//wMAAAALAAYAAAD//wQAAAALAAYAAAD//wUAAAALAAYAAAAAAAMAAAAKAAYAAAAAAAQAAAAKAAYAAAAAAAUAAAAKAAYAAAABAAMAAAALAAYAAAABAAQAAAALAAYAAAABAAUAAAALAAYAAAACAAMAAAAKAAYAAAACAAQAAAAKAAYAAAACAAUAAAAKAAYAAAD//wEAAAAKAAUAAAD//wIAAAALAAYAAAAAAAEAAAALAAUAAAAAAAIAAAAKAAYAAAABAAEAAAAKAAUAAAABAAIAAAALAAYAAAACAAEAAAALAAUAAAACAAIAAAAKAAYAAAADAAMAAAALAAYAAAADAAQAAAALAAYAAAADAAUAAAALAAYAAAAEAAMAAAAKAAYAAAAEAAQAAAAKAAYAAAAEAAUAAAAKAAYAAAAFAAMAAAALAAYAAAAFAAQAAAALAAYAAAAFAAUAAAALAAYAAAADAAEAAAAKAAUAAAADAAIAAAALAAYAAAAEAAEAAAALAAUAAAAEAAIAAAAKAAYAAAAFAAEAAAAKAAUAAAAFAAIAAAALAAYAAAD+/wEAAAALAAUAAAD+/wIAAAAKAAYAAAD+/wMAAAAKAAYAAAD+/wQAAAAKAAYAAAD+/wUAAAAKAAYAAAD9/wMAAAAJAAYAAAD9/wQAAAAJAAYAAAD9/wUAAAAJAAcAAAD9/wEAAAAKAAUAAAD9/wIAAAAJAAYAAAA=")
        |tile_set = ExtResource("2_48f77")
        |
        |[node name="Windows-stairs-doors" type="TileMapLayer" parent="."]
        |show_behind_parent = true
        |y_sort_enabled = true
        |position = Vector2(1, -6)
        |tile_set = ExtResource("3_ms3in")
        |
        |[node name="Player" parent="." instance=ExtResource("4_fu6cl")]
        |light_mask = 2
        |position = Vector2(12, 91)
        |
        |[node name="Camera2D" type="Camera2D" parent="Player"]
        |zoom = Vector2(3, 3)
        |position_smoothing_enabled = true
        |""".stripMargin

    Parser.parse(tscn, Parser.Kind.TSCN) match {
      case Right(parsed: PackedScene) =>
        println(parsed)

      case Right(other) =>
        fail(s"Expected PackedScene, got ${other.getClass.getSimpleName}")

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }
}
