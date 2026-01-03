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

ThisBuild / credentials ++= Seq(
  Credentials(
    "Sonatype Nexus Repository Manager",
    "central.sonatype.com",
    sys.env.getOrElse("SONATYPE_USERNAME", ""),
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  ),
)

// PGP signing configuration
pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)

val libraryScalaVersion = "3.7.4"
val sbtPluginScalaVersion = "2.12.21"

lazy val root = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(sbtGodotBuild)

lazy val sbtGodotBuild = (project in file("sbtGodotBuild"))
  .settings(
    name := "sbt-godot-build",
    version := "0.1.0",
    sbtPlugin := true,
    scalaVersion := sbtPluginScalaVersion,
    publishMavenStyle := true,
    sbtPluginPublishLegacyMavenStyle := false,
    publishTo := {
      val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
      if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
      else localStaging.value
    },
  )