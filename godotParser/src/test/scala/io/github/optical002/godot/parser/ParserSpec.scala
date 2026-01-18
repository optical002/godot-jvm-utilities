package io.github.optical002.godot.parser

import io.github.optical002.godot.parser.core.ParseError
import io.github.optical002.godot.parser.model.{ConfigFile, PackedScene, TextResource}

class ParserSpec extends munit.FunSuite {

  test("parse simple .tscn file") {
    val tscn = """[gd_scene load_steps=2 format=4]
[node name="Root" type="Node2D"]
"""

    Parser.parseTscn(tscn) match {
      case Right(parsed) =>
        assertEquals(parsed.loadSteps, 2)
        assertEquals(parsed.format, 4)
        assertEquals(parsed.nodes.length, 1)
        assertEquals(parsed.nodes.head.name, "Root")
        assertEquals(parsed.nodes.head.nodeType, Some("Node2D"))

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

    Parser.parseTres(tres) match {
      case Right(parsed: TextResource) =>
        assertEquals(parsed.resourceType, "Resource")
        assertEquals(parsed.format, 4)
        assertEquals(parsed.properties.size, 2)

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

    Parser.parseConfig(config) match {
      case Right(parsed: ConfigFile) =>
        assertEquals(parsed.sections.size, 2)
        assert(parsed.sections.contains("section1"))
        assert(parsed.sections.contains("section2"))

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("parse Vector2 construct") {
    val tscn = """[gd_scene format=4]
[node name="Test" type="Node"]
position = Vector2(10.5, 20.5)
"""

    Parser.parseTscn(tscn) match {
      case Right(parsed: PackedScene) =>
        val node = parsed.nodes.head
        assert(node.properties.contains("position"))

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

  test("parse Color from hex") {
    val tscn = """[gd_scene format=4]
[node name="Test" type="Node"]
color = #ff0000
"""

    Parser.parseTscn(tscn) match {
      case Right(parsed: PackedScene) =>
        val node = parsed.nodes.head
        assert(node.properties.contains("color"))

      case Left(err) =>
        fail(s"Parse error: ${err.message}")
    }
  }

}
