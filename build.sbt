ThisBuild / organization         := "io.github.optical002"
ThisBuild / organizationName     := "optical002"
ThisBuild / organizationHomepage := Some(url("https://github.com/optical002"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/optical002/godot-jvm-utilities"),     // Browse URL
    "scm:git@github.com:optical002/godot-jvm-utilities.git"       // Connection string
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
ThisBuild / licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / homepage               := Some(url("https://github.com/optical002/godot-jvm-utilities"))
ThisBuild / sonatypeCredentialHost := "central.sonatype.com"
ThisBuild / sonatypeRepository     := "https://central.sonatype.com/api/v1/publisher"
ThisBuild / versionScheme          := Some("early-semver")

// These can't be ThisBuild because they use .value
publishMavenStyle := true
publishTo := sonatypePublishToBundle.value

ThisBuild / credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "central.sonatype.com",
  sys.env.getOrElse("SONATYPE_USERNAME", ""),
  sys.env.getOrElse("SONATYPE_PASSWORD", "")
)

// PGP signing configuration
pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)

// Required for sbt plugins publishing to Maven Central
ThisBuild / sbtPluginPublishLegacyMavenStyle := false

val libraryScalaVersion = "3.7.4"
val sbtPluginScalaVersion = "2.12.21"

lazy val root = (project in file("."))
  .aggregate(sbtGodotBuild)

lazy val sbtGodotBuild = (project in file("sbtGodotBuild"))
  .settings(
    name := "sbt-godot-build",
    version := "0.1.0",
    sbtPlugin := true,
    scalaVersion := sbtPluginScalaVersion
  )