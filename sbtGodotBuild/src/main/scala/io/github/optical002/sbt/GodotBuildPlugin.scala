package io.github.optical002.sbt

import sbt.*

class GodotBuildPlugin extends AutoPlugin {
  override def trigger = noTrigger

  object autoImport {
    val embedJre = taskKey[Unit]("Re-downloads and embeds JRE")
    val dev = taskKey[Unit]("Regenerates .jar file for code changes")
    val godotBuild = taskKey[Unit]("Performs full build")
  }

  private val generateClassGraphEntry = taskKey[Unit]("???")
  private val compileGeneratedEntry = taskKey[Unit]("???")
  private val packageBootstrapJar = taskKey[File]("???")
  private val packageMainJar = taskKey[File]("???")
  private val generateGdIgnoreFiles = taskKey[Unit]("Generates generic '.gdignore' files")


  override lazy val projectSettings = Seq(

  )
}
