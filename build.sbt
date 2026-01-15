import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import scalanative.sbtplugin.ScalaNativePlugin.autoImport.*
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType, *}

ThisBuild / organization         := "io.github.optical002"
ThisBuild / organizationName     := "optical002"
ThisBuild / organizationHomepage := Some(url("https://github.com/optical002"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/optical002/godot-jvm-utilities"),
    "scm:git@github.com:optical002/godot-jvm-utilities.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "optical002",
    name  = "optical002",
    email = "pauliussuku@gmail.com",
    url   = url("https://github.com/optical002")
  )
)
ThisBuild / description            := "Utility packages/plugins for game development with godot."
ThisBuild / licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / homepage               := Some(url("https://github.com/optical002/godot-jvm-utilities"))
ThisBuild / versionScheme          := Some("early-semver")

ThisBuild / credentials ++= Seq(
  Credentials(
    "Sonatype Nexus Repository Manager",
    "central.sonatype.com",
    sys.env.getOrElse("SONATYPE_USERNAME", ""),
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  ),
)
ThisBuild / publishTo := localStaging.value
pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)

val libraryScalaVersion = "3.7.4"
val sbtPluginScalaVersion = "2.12.21"

lazy val root = (project in file("."))
  .settings(
    name := "godot-jvm-utilities",
    publish / skip := true,
    version := "0.1.0"
  )
  .aggregate(
    sbtGodotBuild,
    godotParser.jvm,
    godotParser.native
  )

lazy val sbtGodotBuild = (project in file("sbtGodotBuild"))
  .settings(
    name := "sbt-godot-build",
    version := "0.1.4",
    sbtPlugin := true,
    scalaVersion := sbtPluginScalaVersion,
    publishMavenStyle := true,
    sbtPluginPublishLegacyMavenStyle := false,
  )

lazy val godotParser = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("godotParser"))
  .settings(
    name := "godot-parser",
    version := "0.1.0",
    scalaVersion := libraryScalaVersion,
    publishMavenStyle := true,
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit" % "1.0.0" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.18.0" % Test
    )
  )

lazy val godotParserJVM = godotParser.jvm
lazy val godotParserNative = godotParser.native