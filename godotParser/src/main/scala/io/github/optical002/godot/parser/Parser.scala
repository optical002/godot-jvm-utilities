package io.github.optical002.godot.parser

import io.github.optical002.godot.parser.core.ParseResult
import io.github.optical002.godot.parser.model.Parsed
import io.github.optical002.godot.parser.parser.VariantParser
import io.github.optical002.godot.parser.assembler.{SceneAssembler, ResourceAssembler, ConfigAssembler}

object Parser {
  enum Kind {
    case TSCN, TRES, Config
  }

  def parse(content: String, kind: Kind): ParseResult[Parsed] = {
    // Parse into tags
    VariantParser.parse(content).flatMap { tags =>
      // Assemble into typed structure
      kind match {
        case Kind.TSCN => SceneAssembler.assemble(tags)
        case Kind.TRES => ResourceAssembler.assemble(tags)
        case Kind.Config => ConfigAssembler.assemble(tags)
      }
    }
  }
}
