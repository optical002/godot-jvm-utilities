package io.github.optical002.godot.parser.tokenizer

class CharStream(input: String) {
  private var position: Int = 0
  private var line: Int = 1
  private var column: Int = 1
  private var saved: Option[Char] = None

  //noinspection AccessorLikeMethodIsEmptyParen
  def getChar(): Char = {
    saved match {
      case Some(c) =>
        saved = None
        c
      case None =>
        if (position >= input.length) {
          0.toChar // EOF marker
        } else {
          val c = input.charAt(position)
          position += 1
          if (c == '\n') {
            line += 1
            column = 1
          } else {
            column += 1
          }
          c
        }
    }
  }

  def saveChar(c: Char): Unit = {
    saved = Some(c)
    if (c == '\n') {
      line -= 1
      // Column will be recalculated on next getChar
    } else {
      column -= 1
    }
  }

  def isEof: Boolean = position >= input.length && saved.isEmpty

  def currentLine: Int = line
  def currentColumn: Int = column
  def currentPosition: Int = position

  def getContext(contextSize: Int = 50): String = {
    val start = Math.max(0, position - contextSize)
    val end = Math.min(input.length, position + contextSize)
    val context = input.substring(start, end)
    val marker = " " * Math.min(position - start, contextSize) + "^"
    s"$context\n$marker"
  }
}
