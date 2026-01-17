package io.github.optical002.godot.parser.core

case class Tag(
  name: String,
  fields: Map[String, Variant],
  line: Int
) {
  def getResourceType: String = fields.get("type").flatMap(_.asString).getOrElse("")
  def getScriptClass: Option[String] = fields.get("script_class").flatMap(_.asString)
  def getLoadSteps: Int = fields.get("load_steps").flatMap(_.asInt).map(_.toInt).getOrElse(0)
  def getFormat: Int = fields.get("format").flatMap(_.asInt).map(_.toInt).getOrElse(3)
  def getUid: Option[String] = fields.get("uid").flatMap(_.asString)
}
