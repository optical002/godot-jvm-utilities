package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.{ExtResource, SubResource, TextResource}

object ResourceAssembler {

  def assemble(tags: Vector[Tag]): ParseResult[TextResource] =
    if (tags.isEmpty) {
      Left(ParseError.SemanticError(
        "Empty file - expected [gd_resource] header",
        0,
        "",
        Map.empty
      ))
    } else {
      val headerTag = tags.head
      if (headerTag.name != "gd_resource") {
        Left(ParseError.SemanticError(
          s"Expected [gd_resource] header, got [${headerTag.name}]",
          headerTag.line,
          "",
          Map("expected" -> "gd_resource", "actual" -> headerTag.name)
        ))
      } else {
        val resourceType = headerTag.fields.get("type").flatMap(_.asString).getOrElse("")
        val scriptClass = headerTag.fields.get("script_class").flatMap(_.asString)
        val format = headerTag.fields.get("format").flatMap(_.asInt).map(_.toInt).getOrElse(3)
        val uid = headerTag.fields.get("uid").flatMap(_.asString)

        def parseTag(tag: Tag): ParseResult[Either[String, (String, Any)]] =
          tag.name match {
            case "ext_resource" => parseExtResource(tag).map(r => Right(("ext_resource", r)))
            case "sub_resource" => parseSubResource(tag).map(r => Right(("sub_resource", r)))
            case "resource" => Right(Right(("resource", tag.fields)))
            case other =>
              Left(ParseError.SemanticError(
                s"Unexpected tag in .tres file: [$other]",
                tag.line,
                "",
                Map("tag" -> other)
              ))
          }

        tags.tail.foldLeft[ParseResult[(Vector[ExtResource], Vector[SubResource], Map[String, Variant])]](
          Right((Vector.empty, Vector.empty, Map.empty))
        ) { (acc, tag) =>
          acc.flatMap { case (extRes, subRes, props) =>
            parseTag(tag).map {
              case Right(("ext_resource", r: ExtResource)) => (extRes :+ r, subRes, props)
              case Right(("sub_resource", r: SubResource)) => (extRes, subRes :+ r, props)
              case Right(("resource", fields: Map[_, _])) => (extRes, subRes, fields.asInstanceOf[Map[String, Variant]])
              case _ => (extRes, subRes, props)
            }
          }
        }.map { case (extRes, subRes, props) =>
          TextResource(resourceType, scriptClass, format, uid, extRes, subRes, props)
        }
      }
    }

  private def parseExtResource(tag: Tag): ParseResult[ExtResource] =
    tag.fields.get("id").flatMap(v => v.asString.orElse(v.asInt.map(_.toString))).toRight(
      ParseError.SemanticError(
        "ext_resource missing 'id' field",
        tag.line,
        "",
        Map.empty
      )
    ).map { id =>
      val resourceType = tag.fields.get("type").flatMap(_.asString).getOrElse("")
      val path = tag.fields.get("path").flatMap(_.asString).getOrElse("")
      val uid = tag.fields.get("uid").flatMap(_.asString)
      ExtResource(id, resourceType, path, uid)
    }

  private def parseSubResource(tag: Tag): ParseResult[SubResource] =
    tag.fields.get("id").flatMap(v => v.asString.orElse(v.asInt.map(_.toString))).toRight(
      ParseError.SemanticError(
        "sub_resource missing 'id' field",
        tag.line,
        "",
        Map.empty
      )
    ).map { id =>
      val resourceType = tag.fields.get("type").flatMap(_.asString).getOrElse("")
      val properties = tag.fields - "id" - "type"
      SubResource(id, resourceType, properties)
    }
}
