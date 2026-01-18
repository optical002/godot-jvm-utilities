package io.github.optical002.godot.parser.model

import io.github.optical002.godot.parser.core.Variant

case class ExtResource(
  id: String,
  resourceType: String,
  path: String,
  uid: Option[String]
)

case class SubResource(
  id: String,
  resourceType: String,
  properties: Map[String, Variant]
)
