package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.{ConnectionData, ExtResource, NodeData, PackedScene, SubResource}

object SceneAssembler {

  def assemble(tags: Vector[Tag])(using Context): ParseResult[PackedScene] =
    if (tags.isEmpty) {
      Left(ParseError.SemanticError.a(
        "Empty file - expected [gd_scene] header",
        0,
        Map.empty
      ))
    } else {
      val headerTag = tags.head
      if (headerTag.name != "gd_scene") {
        Left(ParseError.SemanticError.a(
          s"Expected [gd_scene] header, got [${headerTag.name}]",
          headerTag.line,
          Map("expected" -> "gd_scene", "actual" -> headerTag.name)
        ))
      } else {
        val loadSteps = headerTag.getLoadSteps
        val format = headerTag.getFormat
        val uid = headerTag.getUid

        def parseTag(tag: Tag): ParseResult[Either[String, (String, Any)]] =
          tag.name match {
            case "ext_resource" => TagParsers.parseExtResource(tag).map(r => Right(("ext_resource", r)))
            case "sub_resource" => TagParsers.parseSubResource(tag).map(r => Right(("sub_resource", r)))
            case "node" => TagParsers.parseNode(tag).map(r => Right(("node", r)))
            case "connection" => TagParsers.parseConnection(tag).map(r => Right(("connection", r)))
            case "editable" =>
              tag.fields.get("path").flatMap(_.asString).toRight(
                ParseError.SemanticError.a(
                  "editable tag missing 'path' field",
                  tag.line,
                  Map.empty
                )
              ).map(path => Right(("editable", path)))
            case other =>
              Left(ParseError.SemanticError.a(
                s"Unexpected tag in .tscn file: [$other]",
                tag.line,
                Map("tag" -> other)
              ))
          }

        tags.tail.foldLeft[ParseResult[(
          Vector[ExtResource],
          Vector[SubResource],
          Vector[NodeData],
          Vector[ConnectionData],
          Vector[String]
        )]](
          Right((Vector.empty, Vector.empty, Vector.empty, Vector.empty, Vector.empty))
        ) { (acc, tag) =>
          acc.flatMap { case (extRes, subRes, nodes, conns, editables) =>
            parseTag(tag).map {
              case Right(("ext_resource", r: ExtResource)) => (extRes :+ r, subRes, nodes, conns, editables)
              case Right(("sub_resource", r: SubResource)) => (extRes, subRes :+ r, nodes, conns, editables)
              case Right(("node", n: NodeData)) => (extRes, subRes, nodes :+ n, conns, editables)
              case Right(("connection", c: ConnectionData)) => (extRes, subRes, nodes, conns :+ c, editables)
              case Right(("editable", path: String)) => (extRes, subRes, nodes, conns, editables :+ path)
              case _ => (extRes, subRes, nodes, conns, editables)
            }
          }
        }.map { case (extRes, subRes, nodes, conns, editables) =>
          PackedScene(loadSteps, format, uid, extRes, subRes, nodes, conns, editables)
        }
      }
    }

}
