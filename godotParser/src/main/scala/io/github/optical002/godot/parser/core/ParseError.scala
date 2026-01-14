package io.github.optical002.godot.parser.core

sealed trait ParseError {
  def message: String
  def line: Int
  def context: String
}

object ParseError {
  case class TokenizeError(
    message: String,
    line: Int,
    column: Int,
    context: String
  ) extends ParseError

  case class SyntaxError(
    message: String,
    line: Int,
    context: String,
    expected: Option[String] = None,
    actual: Option[String] = None
  ) extends ParseError

  case class SemanticError(
    message: String,
    line: Int,
    context: String,
    details: Map[String, String] = Map.empty
  ) extends ParseError

  case class UnsupportedVersion(
    formatVersion: Int,
    maxSupported: Int,
    line: Int,
    context: String
  ) extends ParseError {
    override def message: String = s"Unsupported format version $formatVersion (max supported: $maxSupported)"
  }
}

type ParseResult[T] = Either[ParseError, T]
