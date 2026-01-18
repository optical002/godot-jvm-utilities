package io.github.optical002.godot.parser.core

sealed trait ParseError {
  def message: String
  def line: Int
  def maybeColumn: Option[Int]
  def ctx: Context
  
  def formattedMessage: String = {
    val errorType = this match {
      case _: ParseError.TokenizeError => "Tokenize error"
      case _: ParseError.SyntaxError => "Syntax error"
      case _: ParseError.SemanticError => "Semantic error"
      case _: ParseError.UnsupportedVersion => "Unsupported version"
    }

    // Get source lines
    val lines = ctx.source.split("\n")
    val sourceLine = if (line > 0 && line <= lines.length) lines(line - 1) else ""

    // Calculate line number width for padding
    val lineNumWidth = math.max(3, line.toString.length)
    val padding = " " * (lineNumWidth + 1)

    // Build the error message
    val sb = new StringBuilder

    // Header: error type
    sb.append(s"\u001b[1;31merror\u001b[0m: $errorType\n")

    // Location line
    sb.append(s"  \u001b[1;34m-->\u001b[0m ${ctx.fileName}:$line")
    maybeColumn.foreach(col => sb.append(s":$col"))
    sb.append("\n")

    // Empty separator line
    sb.append(s"$padding\u001b[1;34m|\u001b[0m\n")

    // Source line with line number
    if (sourceLine.nonEmpty) {
      sb.append(s"\u001b[1;34m${line.toString.padTo(lineNumWidth, ' ')}\u001b[0m \u001b[1;34m|\u001b[0m $sourceLine\n")

      // Error pointer line
      maybeColumn match {
        case Some(col) if col > 0 =>
          val pointer = " " * (col - 1) + "\u001b[1;31m^\u001b[0m"
          sb.append(s"$padding\u001b[1;34m|\u001b[0m $pointer")

          // Add message on the same line if short enough
          if (message.length < 40) {
            sb.append(s" \u001b[1;31m$message\u001b[0m")
          }
          sb.append("\n")

        case _ =>
          // No column info, just show underline for whole line
          val underline = "\u001b[1;31m" + ("^" * math.min(sourceLine.length, 80)) + "\u001b[0m"
          sb.append(s"$padding\u001b[1;34m|\u001b[0m $underline\n")
      }
    }

    // Empty separator line
    sb.append(s"$padding\u001b[1;34m|\u001b[0m\n")

    // Message (if not already shown on pointer line)
    if (maybeColumn.isEmpty || message.length >= 40) {
      sb.append(s"$padding\u001b[1;33m=\u001b[0m \u001b[1m$message\u001b[0m\n")
    }

    // Additional context based on error type
    this match {
      case err: ParseError.SyntaxError =>
        err.expected.foreach { exp =>
          sb.append(s"$padding\u001b[1;33m=\u001b[0m expected: \u001b[1;32m$exp\u001b[0m\n")
        }
        err.actual.foreach { act =>
          sb.append(s"$padding\u001b[1;33m=\u001b[0m actual: \u001b[1;36m$act\u001b[0m\n")
        }

      case err: ParseError.SemanticError =>
        if (err.details.nonEmpty) {
          sb.append(s"$padding\u001b[1;33m=\u001b[0m details:\n")
          err.details.foreach { case (k, v) =>
            sb.append(s"$padding  \u001b[1;36m$k\u001b[0m: $v\n")
          }
        }

      case err: ParseError.UnsupportedVersion =>
        sb.append(s"$padding\u001b[1;33m=\u001b[0m format version: \u001b[1;36m${err.formatVersion}\u001b[0m\n")
        sb.append(s"$padding\u001b[1;33m=\u001b[0m max supported: \u001b[1;32m${err.maxSupported}\u001b[0m\n")

      case _ => ()
    }

    sb.toString()
  }
}

object ParseError {
  case class TokenizeError private (
    message: String,
    line: Int,
    column: Int,
    ctx: Context
  ) extends ParseError {
    override def maybeColumn: Option[Int] = Some(column)
  }
  object TokenizeError {
    def a(
      message: String,
      line: Int,
      column: Int,
    )(using ctx: Context): TokenizeError = TokenizeError(
      message = message, line = line, column = column, ctx = ctx
    )
  }

  case class SyntaxError private (
    message: String,
    line: Int,
    ctx: Context,
    expected: Option[String] = None,
    actual: Option[String] = None
  ) extends ParseError {
    override def maybeColumn: Option[Int] = None
  }
  object SyntaxError {
    def a(
      message: String,
      line: Int,
      expected: Option[String] = None,
      actual: Option[String] = None
    )(using ctx: Context): SyntaxError = SyntaxError(
      message = message, line = line, ctx = ctx, expected = expected, actual = actual
    )
  }

  case class SemanticError private (
    message: String,
    line: Int,
    ctx: Context,
    details: Map[String, String] = Map.empty
  ) extends ParseError {
    override def maybeColumn: Option[Int] = None
  }
  object SemanticError {
    def a(
      message: String,
      line: Int,
      details: Map[String, String] = Map.empty
    )(using ctx: Context): SemanticError = SemanticError(
      message = message, line = line, ctx = ctx, details = details
    )
  }

  case class UnsupportedVersion private (
    formatVersion: Int,
    maxSupported: Int,
    line: Int,
    ctx: Context
  ) extends ParseError {
    override def message: String = s"Unsupported format version $formatVersion (max supported: $maxSupported)"
    override def maybeColumn: Option[Int] = None
  }
  object UnsupportedVersion {
    def a(
      formatVersion: Int,
      maxSupported: Int,
      line: Int
    )(using ctx: Context): UnsupportedVersion = UnsupportedVersion(
      formatVersion = formatVersion, maxSupported = maxSupported, line = line, ctx = ctx
    )
  }
}

type ParseResult[T] = Either[ParseError, T]
