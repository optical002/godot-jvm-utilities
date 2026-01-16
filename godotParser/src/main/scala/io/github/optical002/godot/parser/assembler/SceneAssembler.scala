package io.github.optical002.godot.parser.assembler

import io.github.optical002.godot.parser.core.*
import io.github.optical002.godot.parser.model.{ConnectionData, ExtResource, NodeData, PackedScene, SubResource}

object SceneAssembler {

  def assemble(tags: Vector[Tag]): ParseResult[PackedScene] = {
    // First tag should be [gd_scene ...]
    if (tags.isEmpty) {
      return Left(ParseError.SemanticError(
        "Empty file - expected [gd_scene] header",
        0,
        "",
        Map.empty
      ))
    }

    val headerTag = tags.head
    if (headerTag.name != "gd_scene") {
      return Left(ParseError.SemanticError(
        s"Expected [gd_scene] header, got [${headerTag.name}]",
        headerTag.line,
        "",
        Map("expected" -> "gd_scene", "actual" -> headerTag.name)
      ))
    }

    // Extract header fields
    val loadSteps = headerTag.fields.get("load_steps").flatMap(_.asInt).map(_.toInt).getOrElse(0)
    val format = headerTag.fields.get("format").flatMap(_.asInt).map(_.toInt).getOrElse(3)
    val uid = headerTag.fields.get("uid").flatMap(_.asString)

    // Parse remaining tags
    val extResources = Vector.newBuilder[ExtResource]
    val subResources = Vector.newBuilder[SubResource]
    val nodes = Vector.newBuilder[NodeData]
    val connections = Vector.newBuilder[ConnectionData]
    val editableInstances = Vector.newBuilder[String]

    for (tag <- tags.tail)
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

        case "node" =>
          parseNode(tag) match {
            case Right(node) => nodes += node
            case Left(err) => return Left(err)
          }

        case "connection" =>
          parseConnection(tag) match {
            case Right(conn) => connections += conn
            case Left(err) => return Left(err)
          }

        case "editable" =>
          tag.fields.get("path").flatMap(_.asString) match {
            case Some(path) => editableInstances += path
            case None =>
              return Left(ParseError.SemanticError(
                "editable tag missing 'path' field",
                tag.line,
                "",
                Map.empty
              ))
          }

        case other =>
          return Left(ParseError.SemanticError(
            s"Unexpected tag in .tscn file: [$other]",
            tag.line,
            "",
            Map("tag" -> other)
          ))
      }

    Right(PackedScene(
      loadSteps = loadSteps,
      format = format,
      uid = uid,
      extResources = extResources.result(),
      subResources = subResources.result(),
      nodes = nodes.result(),
      connections = connections.result(),
      editableInstances = editableInstances.result()
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

  private def parseConnection(tag: Tag): ParseResult[ConnectionData] = {
    val signal = tag.fields.get("signal").flatMap(_.asString).getOrElse {
      return Left(ParseError.SemanticError(
        "connection missing 'signal' field",
        tag.line,
        "",
        Map.empty
      ))
    }

    val from = tag.fields.get("from").flatMap(_.asString).getOrElse {
      return Left(ParseError.SemanticError(
        "connection missing 'from' field",
        tag.line,
        "",
        Map.empty
      ))
    }

    val to = tag.fields.get("to").flatMap(_.asString).getOrElse {
      return Left(ParseError.SemanticError(
        "connection missing 'to' field",
        tag.line,
        "",
        Map.empty
      ))
    }

    val method = tag.fields.get("method").flatMap(_.asString).getOrElse {
      return Left(ParseError.SemanticError(
        "connection missing 'method' field",
        tag.line,
        "",
        Map.empty
      ))
    }

    val flags = tag.fields.get("flags").flatMap(_.asInt).map(_.toInt).getOrElse(0)
    val unbinds = tag.fields.get("unbinds").flatMap(_.asInt).map(_.toInt).getOrElse(0)

    val binds = tag.fields.get("binds").flatMap(_.asArray).getOrElse(Vector.empty)

    val fromIdPath = tag.fields.get("from_uid_path").flatMap(_.asArray).map { arr =>
      arr.flatMap(_.asInt).map(_.toInt)
    }.getOrElse(Vector.empty)

    val toIdPath = tag.fields.get("to_uid_path").flatMap(_.asArray).map { arr =>
      arr.flatMap(_.asInt).map(_.toInt)
    }.getOrElse(Vector.empty)

    Right(ConnectionData(
      signal = signal,
      from = from,
      fromIdPath = fromIdPath,
      to = to,
      toIdPath = toIdPath,
      method = method,
      flags = flags,
      binds = binds,
      unbinds = unbinds
    ))
  }
}
