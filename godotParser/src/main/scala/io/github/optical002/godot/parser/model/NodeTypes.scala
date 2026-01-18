package io.github.optical002.godot.parser.model

import io.github.optical002.godot.parser.core.Variant

case class NodeData(
  name: String,
  nodeType: Option[String],
  parent: Option[String],
  parentIdPath: Vector[Int],
  owner: Option[String],
  ownerIdPath: Vector[Int],
  index: Option[Int],
  uniqueId: Option[Int],
  instance: Option[String],
  instancePlaceholder: Option[String],
  groups: Vector[String],
  nodePaths: Vector[String],
  properties: Map[String, Variant]
)

case class ConnectionData(
  signal: String,
  from: String,
  fromIdPath: Vector[Int],
  to: String,
  toIdPath: Vector[Int],
  method: String,
  flags: Int,
  binds: Vector[Variant],
  unbinds: Int
)
