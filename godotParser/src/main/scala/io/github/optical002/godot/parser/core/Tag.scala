package io.github.optical002.godot.parser.core

case class Tag(
  name: String,
  fields: Map[String, Variant],
  line: Int
)
