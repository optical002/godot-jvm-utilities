package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.{ConnectionData, ExtResource, NodeData, PackedScene, SubResource}

object SceneAssembler {

  def assemble(tags: Vector[Tag]): ParseResult[PackedScene] =
    if (tags.isEmpty) {
      Left(ParseError.SemanticError(
        "Empty file - expected [gd_scene] header",
        0,
        "",
        Map.empty
      ))
    } else {
      val headerTag = tags.head
      if (headerTag.name != "gd_scene") {
        Left(ParseError.SemanticError(
          s"Expected [gd_scene] header, got [${headerTag.name}]",
          headerTag.line,
          "",
          Map("expected" -> "gd_scene", "actual" -> headerTag.name)
        ))
      } else {
        val loadSteps = headerTag.fields.get("load_steps").flatMap(_.asInt).map(_.toInt).getOrElse(0)
        val format = headerTag.fields.get("format").flatMap(_.asInt).map(_.toInt).getOrElse(3)
        val uid = headerTag.fields.get("uid").flatMap(_.asString)

        def parseTag(tag: Tag): ParseResult[Either[String, (String, Any)]] =
          tag.name match {
            case "ext_resource" => parseExtResource(tag).map(r => Right(("ext_resource", r)))
            case "sub_resource" => parseSubResource(tag).map(r => Right(("sub_resource", r)))
            case "node" => parseNode(tag).map(r => Right(("node", r)))
            case "connection" => parseConnection(tag).map(r => Right(("connection", r)))
            case "editable" =>
              tag.fields.get("path").flatMap(_.asString).toRight(
                ParseError.SemanticError(
                  "editable tag missing 'path' field",
                  tag.line,
                  "",
                  Map.empty
                )
              ).map(path => Right(("editable", path)))
            case other =>
              Left(ParseError.SemanticError(
                s"Unexpected tag in .tscn file: [$other]",
                tag.line,
                "",
                Map("tag" -> other)
              ))
          }

        tags.tail.foldLeft[ParseResult[(Vector[ExtResource], Vector[SubResource], Vector[NodeData], Vector[ConnectionData], Vector[String])]](
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
      // Remove metadata fields from properties
      val properties = tag.fields - "id" - "type"
      SubResource(id, resourceType, properties)
    }

  private def parseNode(tag: Tag): ParseResult[NodeData] = {
    val nameResult = tag.fields.get("name").flatMap(_.asString).toRight {
      ParseError.SemanticError(
        "node missing 'name' field",
        tag.line,
        "",
        Map.empty
      )
    }

    nameResult.map { name =>
      val nodeType = tag.fields.get("type").flatMap(_.asString)
      val parent = tag.fields.get("parent").flatMap(_.asString)
      val owner = tag.fields.get("owner").flatMap(_.asString)
      val index = tag.fields.get("index").flatMap(_.asInt).map(_.toInt)
      val uniqueId = tag.fields.get("unique_id").flatMap(_.asInt).map(_.toInt)
      // instance is an ExtResource reference
      val instance = tag.fields.get("instance").flatMap(_.asExtResource)
      // instance_placeholder is a string path
      val instancePlaceholder = tag.fields.get("instance_placeholder").flatMap(_.asString)

      // Parse groups
      val groups = tag.fields.get("groups").flatMap(_.asArray).map { arr =>
        arr.flatMap(_.asString)
      }.getOrElse(Vector.empty)

      // Parse node_paths
      val nodePaths = tag.fields.get("node_paths").flatMap(_.asArray).map { arr =>
        arr.flatMap(_.asString)
      }.getOrElse(Vector.empty)

      // Parse parent_id_path and owner_uid_path
      val parentIdPath = tag.fields.get("parent_id_path").flatMap(_.asArray).map { arr =>
        arr.flatMap(_.asInt).map(_.toInt)
      }.getOrElse(Vector.empty)

      val ownerIdPath = tag.fields.get("owner_uid_path").flatMap(_.asArray).map { arr =>
        arr.flatMap(_.asInt).map(_.toInt)
      }.getOrElse(Vector.empty)

      // Remove metadata fields from properties
      val properties = tag.fields - "name" - "type" - "parent" - "owner" - "index" -
        "unique_id" - "instance" - "instance_placeholder" - "groups" - "node_paths" -
        "parent_id_path" - "owner_uid_path"

      NodeData(
        name = name,
        nodeType = nodeType,
        parent = parent,
        parentIdPath = parentIdPath,
        owner = owner,
        ownerIdPath = ownerIdPath,
        index = index,
        uniqueId = uniqueId,
        instance = instance,
        instancePlaceholder = instancePlaceholder,
        groups = groups,
        nodePaths = nodePaths,
        properties = properties
      )
    }
  }

  private def parseConnection(tag: Tag): ParseResult[ConnectionData] =
    for {
      signal <- tag.fields.get("signal").flatMap(_.asString).toRight(
        ParseError.SemanticError(
          "connection missing 'signal' field",
          tag.line,
          "",
          Map.empty
        )
      )
      from <- tag.fields.get("from").flatMap(_.asString).toRight(
        ParseError.SemanticError(
          "connection missing 'from' field",
          tag.line,
          "",
          Map.empty
        )
      )
      to <- tag.fields.get("to").flatMap(_.asString).toRight(
        ParseError.SemanticError(
          "connection missing 'to' field",
          tag.line,
          "",
          Map.empty
        )
      )
      method <- tag.fields.get("method").flatMap(_.asString).toRight(
        ParseError.SemanticError(
          "connection missing 'method' field",
          tag.line,
          "",
          Map.empty
        )
      )
    } yield {
      val flags = tag.fields.get("flags").flatMap(_.asInt).map(_.toInt).getOrElse(0)
      val unbinds = tag.fields.get("unbinds").flatMap(_.asInt).map(_.toInt).getOrElse(0)
      val binds = tag.fields.get("binds").flatMap(_.asArray).getOrElse(Vector.empty)
      val fromIdPath = tag.fields.get("from_uid_path").flatMap(_.asArray).map(arr =>
        arr.flatMap(_.asInt).map(_.toInt)
      ).getOrElse(Vector.empty)
      val toIdPath = tag.fields.get("to_uid_path").flatMap(_.asArray).map(arr =>
        arr.flatMap(_.asInt).map(_.toInt)
      ).getOrElse(Vector.empty)

      ConnectionData(signal, from, fromIdPath, to, toIdPath, method, flags, binds, unbinds)
    }
}
