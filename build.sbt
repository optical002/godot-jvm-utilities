organization         := "io.github.optical002"
organizationName     := "optical002"
organizationHomepage := Some(url("https://github.com/optical002"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/optical002/godot-jvm-utilities"),     // Browse URL
    "scm:git@github.com:optical002/godot-jvm-utilities.git"       // Connection string
  )
)
developers := List(
  Developer(
    id    = "optical002",
    name  = "optical002",
    email = "pauliussuku@gmail.com",
    url   = url("https://github.com/optical002")
  )
)
licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage               := Some(url("https://github.com/optical002/godot-jvm-utilities"))
sonatypeCredentialHost := "central.sonatype.com"
sonatypeRepository     := "https://central.sonatype.com/api/v1/publisher"
publishMavenStyle      := true
publishTo              := sonatypePublishToBundle.value

credentials += Credentials(
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
  .settings(publish / skip := true)
  .aggregate(sbtGodotBuild)

lazy val sbtGodotBuild = (project in file("sbtGodotBuild"))
  .settings(
    name := "sbt-godot-build",
    version := "0.1.0",
    sbtPlugin := true,
    scalaVersion := sbtPluginScalaVersion
  )