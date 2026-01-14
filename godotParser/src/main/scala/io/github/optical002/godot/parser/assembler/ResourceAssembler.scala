package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.{TextResource, ExtResource, SubResource}

object ResourceAssembler {

  def assemble(tags: Vector[Tag]): ParseResult[TextResource] = {
    // First tag should be [gd_resource ...]
    if (tags.isEmpty) {
      return Left(ParseError.SemanticError(
        "Empty file - expected [gd_resource] header",
        0,
        "",
        Map.empty
      ))
    }

    val headerTag = tags.head
    if (headerTag.name != "gd_resource") {
      return Left(ParseError.SemanticError(
        s"Expected [gd_resource] header, got [${headerTag.name}]",
        headerTag.line,
        "",
        Map("expected" -> "gd_resource", "actual" -> headerTag.name)
      ))
    }

    // Extract header fields
    val resourceType = headerTag.fields.get("type").flatMap(_.asString).getOrElse("")
    val scriptClass = headerTag.fields.get("script_class").flatMap(_.asString)
    val format = headerTag.fields.get("format").flatMap(_.asInt).map(_.toInt).getOrElse(3)
    val uid = headerTag.fields.get("uid").flatMap(_.asString)

    // Parse remaining tags
    val extResources = Vector.newBuilder[ExtResource]
    val subResources = Vector.newBuilder[SubResource]
    var resourceProperties = Map.empty[String, Variant]

    for (tag <- tags.tail) {
      tag.name match {
        case "ext_resource" =>
          parseExtResource(tag) match {
            case Right(extRes) => extResources += extRes
            case Left(err) => return Left(err)
          }

        case "sub_resource" =>
          parseSubResource(tag) match {
            case Right(subRes) => subResources += subRes
            case Left(err) => return Left(err)
          }

        case "resource" =>
          resourceProperties = tag.fields

        case other =>
          return Left(ParseError.SemanticError(
            s"Unexpected tag in .tres file: [$other]",
            tag.line,
            "",
            Map("tag" -> other)
          ))
      }
    }

    Right(TextResource(
      resourceType = resourceType,
      scriptClass = scriptClass,
      format = format,
      uid = uid,
      extResources = extResources.result(),
      subResources = subResources.result(),
      properties = resourceProperties
    ))
  }

  private def parseExtResource(tag: Tag): ParseResult[ExtResource] = {
    val id = tag.fields.get("id").flatMap(v => v.asString.orElse(v.asInt.map(_.toString))).getOrElse {
      return Left(ParseError.SemanticError(
        "ext_resource missing 'id' field",
        tag.line,
        "",
        Map.empty
      ))
    }

    val resourceType = tag.fields.get("type").flatMap(_.asString).getOrElse("")
    val path = tag.fields.get("path").flatMap(_.asString).getOrElse("")
    val uid = tag.fields.get("uid").flatMap(_.asString)

    Right(ExtResource(id, resourceType, path, uid))
  }

  private def parseSubResource(tag: Tag): ParseResult[SubResource] = {
    val id = tag.fields.get("id").flatMap(v => v.asString.orElse(v.asInt.map(_.toString))).getOrElse {
      return Left(ParseError.SemanticError(
        "sub_resource missing 'id' field",
        tag.line,
        "",
        Map.empty
      ))
    }

    val resourceType = tag.fields.get("type").flatMap(_.asString).getOrElse("")

    // Remove metadata fields from properties
    val properties = tag.fields - "id" - "type"

    Right(SubResource(id, resourceType, properties))
  }
}
