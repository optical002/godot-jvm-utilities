package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.ConfigFile

object ConfigAssembler {

  def assemble(tags: Vector[Tag]): ParseResult[ConfigFile] = {
    val sections = scala.collection.mutable.Map[String, Map[String, Variant]]()

    for (tag <- tags)
      sections(tag.name) = tag.fields

    Right(ConfigFile(sections.toMap))
  }
}
