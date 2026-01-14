package io.github.optical002.godot.parser.model

import io.github.optical002.godot.parser.core.Variant

sealed trait Parsed

case class PackedScene(
  loadSteps: Int,
  format: Int,
  uid: Option[String],
  extResources: Vector[ExtResource],
  subResources: Vector[SubResource],
  nodes: Vector[NodeData],
  connections: Vector[ConnectionData],
  editableInstances: Vector[String]
) extends Parsed

case class TextResource(
  resourceType: String,
  scriptClass: Option[String],
  format: Int,
  uid: Option[String],
  extResources: Vector[ExtResource],
  subResources: Vector[SubResource],
  properties: Map[String, Variant]
) extends Parsed

case class ConfigFile(
  sections: Map[String, Map[String, Variant]]
) extends Parsed
