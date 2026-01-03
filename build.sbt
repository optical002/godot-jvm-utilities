organization      := "io.github.optical002"
licenses          := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage          := Some(url("https://github.com/optical002/godot-jvm-utilities"))
publishMavenStyle := true

val libraryScalaVersion = "3.7.4"
val sbtPluginScalaVersion = "2.12.21"

lazy val root = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(sbtGodotBuild)

lazy val sbtGodotBuild = (project in file("sbtGodotBuild"))
  .settings(
    name := "sbt-godot-build",
    sbtPlugin := true,
    scalaVersion := sbtPluginScalaVersion
  )