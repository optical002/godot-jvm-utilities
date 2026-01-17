package io.github.optical002.godot.parser.core

class Context(input: String) {
  def getLine(number: Int): String = input.split("\n")(number)
}
