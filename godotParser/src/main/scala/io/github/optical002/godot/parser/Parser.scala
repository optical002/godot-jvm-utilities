package io.github.optical002.godot.parser

import io.github.optical002.godot.parser.core.ParseResult
import io.github.optical002.godot.parser.model.{ConfigFile, PackedScene, TextResource}
import io.github.optical002.godot.parser.parser.VariantParser
import io.github.optical002.godot.parser.assembler.{ConfigAssembler, ResourceAssembler, SceneAssembler}

object Parser {
  def parseTscn(content: String): ParseResult[PackedScene] = 
    VariantParser.parse(content).flatMap(SceneAssembler.assemble)
  
  def parseTres(content: String): ParseResult[TextResource] =
    VariantParser.parse(content).flatMap(ResourceAssembler.assemble)
    
  def parseConfig(content: String): ParseResult[ConfigFile] =
    VariantParser.parse(content).flatMap(ConfigAssembler.assemble)
}
