package io.github.optical002.godot.parser.core

enum TokenType {
  case CurlyBracketOpen, CurlyBracketClose // { }
  case BracketOpen, BracketClose // [ ]
  case ParenthesisOpen, ParenthesisClose // ( )
  case Identifier // variable_name
  case String, StringName // "text", &"name"
  case Number // 123, 1.5, 1e10
  case Color // #ff00ff
  case Colon, Comma, Period, Equal // : , . =
  case EOF
  case Error
}

case class Token(
  tokenType: TokenType,
  value: Variant,
  line: Int,
  column: Int
)
