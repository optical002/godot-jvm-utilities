package io.github.optical002.godot.parser.core

class Context(val source: String, val fileName: String = "<input>") {
  def getLine(number: Int): String = source.split("\n")(number)
}
