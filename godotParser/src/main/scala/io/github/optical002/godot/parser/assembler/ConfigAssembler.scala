package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.ConfigFile

object ConfigAssembler {

  def assemble(tags: Vector[Tag]): ParseResult[ConfigFile] = {
    Right(ConfigFile(tags.map { tag =>
      tag.name -> tag.fields
    }.toMap))
  }
}
