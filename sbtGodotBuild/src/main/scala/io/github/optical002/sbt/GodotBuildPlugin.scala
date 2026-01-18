package io.github.optical002.sbt

import scala.sys.process.Process

import sbt.*
import sbt.Keys.*

object GodotBuildPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val jreVersion = settingKey[String]("JRE version for godot to use")

    val downloadMavenDeps = taskKey[Unit]("Downloads and extracts Maven dependencies")
    val embedJre = taskKey[Unit]("Re-downloads and embeds JRE")
    val godotBuild = taskKey[Unit]("Performs full build")
    val dev = taskKey[Unit]("Regenerates .jar file for code changes")
  }

  private val godotKotlinVersion = settingKey[String]("")
  private val kotlinVersion = settingKey[String]("")
  private val kotlinCompilerVersion = settingKey[String]("")
  private val classGraphVersion = settingKey[String]("")

  private val generateClassGraphEntry = taskKey[File]("")
  private val compileClassGraphEntry = taskKey[File]("")
  private val packageBootstrapJar = taskKey[File]("")
  private val packageMainJar = taskKey[File]("")
  private val generateGdIgnoreFiles = taskKey[Unit]("Generates generic '.gdignore' files")

  import autoImport.*

  private case class PlatformSettings(
    platform: String,
    arch: String,
    jreExt: String,
    jreFileName: String
  )

  private lazy val godotBootstrapJarFileName = "godot-bootstrap.jar"
  private lazy val mainJarFileName = "main.jar"
  private lazy val platformSettings = {
    val osName = System.getProperty("os.name").toLowerCase()
    val osArch = System.getProperty("os.arch").toLowerCase()
    val isArm = osArch.contains("aarch64") || osArch.contains("arm")
    val arch =
      if (isArm) "aarch64"
      else "x64"
    val jreFileName =
      if (isArm) "jre-arm64-linux"
      else "jre-amd64-linux"

    osName match {
      case "linux" => PlatformSettings(platform = "linux", arch = arch, jreExt = "tar.gz", jreFileName = jreFileName)
      case "mac" => PlatformSettings(platform = "mac", arch = arch, jreExt = "tar.gz", jreFileName = jreFileName)
      case "win" => PlatformSettings(platform = "win", arch = arch, jreExt = "zip", jreFileName = jreFileName)
      case _ => throw new Exception(s"Unsupported OS: $osName")
    }
  }

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    jreVersion := "17.0.13+11",

    // Download Maven dependencies before resolving if they don't exist
    onLoad in Global := {
      val previous = (onLoad in Global).value
      previous.andThen { state =>
        val extracted = Project.extract(state)
        val baseDir = extracted.get(LocalRootProject / baseDirectory)
        val targetDir = extracted.get(LocalRootProject / target)
        val m2Dir = baseDir / ".m2"

        if (!m2Dir.exists()) {
          val log = extracted.get(sLog)
          log.info("[onLoad] .m2 directory not found, downloading Maven dependencies...")

          val downloadDir = targetDir / "download"
          IO.createDirectory(downloadDir)

          val url = "https://github.com/optical002/godot-jvm-utilities/releases/download/dependencies/m2.zip"
          val zipFile = downloadDir / "m2.zip"

          log.info(s"[onLoad] Downloading from $url")
          Process(Seq("curl", "-L", "-o", zipFile.getAbsolutePath, url)).!

          log.info("[onLoad] Extracting Maven dependencies...")
          Process(Seq("unzip", "-q", zipFile.getAbsolutePath, "-d", baseDir.getAbsolutePath)).!

          log.info("[onLoad] Maven dependencies installed successfully")
        }
        state
      }
    },
    resolvers += "Local M2 Repository" at s"file://${(LocalRootProject / baseDirectory).value}/.m2/repository",

    // Leave this as private to plugin only for now, later on after moving to official plugins will need to remove
    // prebuilt jars and coping them and resolver, after that, make this public, so users can change it, independent of
    // the plugin
    godotKotlinVersion := "0.14.3-4.5.1-92ac75f-SNAPSHOT",
    kotlinVersion := "1.9.0",
    kotlinCompilerVersion := "2.1.10",
    classGraphVersion := "4.8.179",
    libraryDependencies ++= Seq(
      "com.utopia-rise" % "godot-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-api-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-core-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-extension-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-internal-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-bootstrap-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-coroutine-library-debug" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "common" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-build-props" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-class-graph-symbol-processor" % godotKotlinVersion.value % "provided",
      "com.utopia-rise" % "godot-entry-generator" % godotKotlinVersion.value % "provided",
      "io.github.classgraph" % "classgraph" % classGraphVersion.value % "provided",
      "com.squareup" % "kotlinpoet" % "1.14.2" % "provided",
      "org.jetbrains.kotlin" % "kotlin-stdlib" % kotlinVersion.value % "provided",
      "org.jetbrains.kotlin" % "kotlin-compiler" % kotlinCompilerVersion.value % "provided",
      "org.jetbrains.kotlin" % "kotlin-reflect" % kotlinCompilerVersion.value % "provided",
      "org.jetbrains.kotlin" % "kotlin-script-runtime" % kotlinCompilerVersion.value % "provided",
      "org.jetbrains.kotlinx" % "kotlinx-coroutines-core-jvm" % "1.10.1" % "provided"
    ),
    downloadMavenDeps := {
      val log = streams.value.log
      val rootBaseDir = (LocalRootProject / baseDirectory).value
      val m2Dir = rootBaseDir / ".m2"

      IO.delete(m2Dir)
      IO.createDirectory(m2Dir)
      val downloadDir = (LocalRootProject / target).value / "download"
      IO.createDirectory(downloadDir)

      val url = "https://github.com/optical002/godot-jvm-utilities/releases/download/dependencies/m2.zip"
      val zipFile = downloadDir / "m2.zip"

      log.info(s"[downloadMavenDeps] Downloading Maven dependencies from $url")
      Process(Seq("curl", "-L", "-o", zipFile.getAbsolutePath, url)).!

      log.info("[downloadMavenDeps] Extracting Maven dependencies...")
      Process(Seq("unzip", "-q", zipFile.getAbsolutePath, "-d", rootBaseDir.getAbsolutePath)).!
      ()
    },
    embedJre := {
      val log = streams.value.log
      val jvmDir = baseDirectory.value / "jvm"
      val jreDir = jvmDir / platformSettings.jreFileName

      IO.delete(jvmDir)
      IO.createDirectory(jvmDir)
      val downloadDir = target.value / "download"
      IO.createDirectory(downloadDir)

      val jdkVersionEncoded = jreVersion.value.replace("+", "%2B")
      val url =
        s"https://api.adoptium.net/v3/binary/version/jdk-$jdkVersionEncoded/${platformSettings.platform}/" +
          s"${platformSettings.arch}/jre/hotspot/normal/eclipse?project=jdk"
      val archiveFile = downloadDir / s"openjdk-17-jre.${platformSettings.jreExt}"

      log.info(s"[embedJre] Downloading OpenJDK from $url")
      Process(Seq("curl", "-L", "-o", archiveFile.getAbsolutePath, url)).!

      val extractDir = downloadDir / "extracted"
      IO.createDirectory(extractDir)

      if (platformSettings.jreExt == "tar.gz") {
        Process(Seq("tar", "xzf", archiveFile.getAbsolutePath, "-C", extractDir.getAbsolutePath)).!
      } else {
        Process(Seq("unzip", "-q", archiveFile.getAbsolutePath, "-d", extractDir.getAbsolutePath)).!
      }

      val extractedJre = IO
        .listFiles(extractDir)
        .find(p => p.isDirectory && (p.getName.contains("jdk") || p.getName.contains("jre")))
        .getOrElse(throw new Exception("Could not find extracted JRE directory!"))

      IO.move(extractedJre, jreDir)
      ()
    },
    godotBuild := Def.taskDyn {
      val log = streams.value.log
      val jvmDir = baseDirectory.value / "jvm"

      log.info("[godotBuild] Building...")

      compileClassGraphEntry.value
      val bootstrapJar = packageBootstrapJar.value
      val mainJar = packageMainJar.value

      val setupTask = if (jvmDir.exists()) {
        Def.task {
          // Clean up existing jar's
          Vector(
            godotBootstrapJarFileName,
            mainJarFileName
          ).foreach { jar =>
            val jarPath = jvmDir / jar
            if (jarPath.exists()) IO.delete(jarPath)
          }
        }
      } else {
        embedJre
      }

      Def.task {
        setupTask.value
        IO.copyFile(bootstrapJar, jvmDir / godotBootstrapJarFileName)
        IO.copyFile(mainJar, jvmDir / mainJarFileName)
        generateGdIgnoreFiles.value
        ()
      }
    }.value,
    dev := Def.taskDyn {
      val jvmDir = baseDirectory.value / "jvm"
      val mainJar = jvmDir / mainJarFileName

      if (mainJar.exists()) {
        Def.task {
          IO.move(mainJar, packageMainJar.value)
          ()
        }
      } else {
        godotBuild
      }
    }.value,
    generateClassGraphEntry := {
      val log = streams.value.log
      val generatedDir = target.value / "generated" / "classgraph"

      // Clean the entire generation directory to force fresh generation
      if (generatedDir.exists()) {
        log.info("[ClassGraph] Cleaning generation directory...")
        IO.delete(generatedDir)
      }
      IO.createDirectory(generatedDir)

      val cp = (Compile / fullClasspath).value.files.toSet
      val processorClasspath = Classpaths.managedJars(Provided, classpathTypes.value, update.value).files

      val projectName = baseDirectory.value.getName
      val regDir = baseDirectory.value / "gdj"

      // Clean existing gdj directory before generating new files
      if (regDir.exists()) {
        log.info("[ClassGraph] Cleaning existing gdj directory...")
        IO.delete(regDir)
      }
      IO.createDirectory(regDir)

      // Also clean the ClassGraph generation cache directory
      val classgraphCacheDir = generatedDir / "main" / "resources" / "entryFiles"
      if (classgraphCacheDir.exists()) {
        log.info("[ClassGraph] Cleaning ClassGraph cache directory...")
        IO.delete(classgraphCacheDir)
      }

      // Code which generates '.gdj' files.
      val wrapperCode = s"""
import godot.annotation.processor.classgraph._
import java.io.File
import scala.jdk.CollectionConverters._
object ClassGraphRunner {
  def main(args: Array[String]): Unit = {
    val projectBaseDir = new File("${baseDirectory.value.getAbsolutePath.replace("\\", "\\\\")}")
    val genDir = new File("${generatedDir.getAbsolutePath.replace("\\", "\\\\")}")
    val settings = new Settings(null, false, "$projectName", projectBaseDir, "gdj", true, true, genDir)
    // Force fresh generation by using timestamp in settings
    System.setProperty("godot.classgraph.timestamp", "${System.currentTimeMillis}")
    val logger = org.slf4j.LoggerFactory.getLogger("ClassGraphProcessor")
    val runtimeClassPath: java.util.Set[File] = ${cp.map(f =>
          "\"" + f.getAbsolutePath.replace("\\", "\\\\") + "\""
        ).mkString(
          "Set(",
          ", ",
          ")"
        )}.map(new File(_)).asJava
    ProcessKt.generateEntryUsingClassGraph(settings, logger, runtimeClassPath)
    println("[ClassGraph] Entry generation complete!")
  }
}
"""

      val wrapperFile = target.value / "ClassGraphRunner.scala"
      IO.write(wrapperFile, wrapperCode)

      try {
        val fullClasspath = (processorClasspath ++ cp).mkString(":")
        val scalaCompilerCp = (Compile / scalaInstance).value.allJars.mkString(":")
        // Compile 'ClassGraphRunner.scala' -> 'ClassGraphRunner.class'
        val compileCmd = Seq(
          "java",
          "-cp",
          scalaCompilerCp,
          "dotty.tools.dotc.Main",
          "-classpath",
          fullClasspath,
          "-d",
          target.value.toString,
          wrapperFile.toString
        )
        Process(compileCmd).!

        // Run 'ClassGraphRunner.class'
        val runCmd = Seq(
          "java",
          "-cp",
          s"${target.value}:$fullClasspath:$scalaCompilerCp",
          "ClassGraphRunner"
        )
        Process(runCmd).!

        val generatedGdjDir = generatedDir / "main" / "resources" / "entryFiles" / "gdj"
        if (generatedGdjDir.exists()) {
          IO.copyDirectory(generatedGdjDir, regDir)
        }
      } catch {
        case e: Exception => log.warn(s"[ClassGraph] Warning: ${e.getMessage}")
      }

      generatedDir
    },
    compileClassGraphEntry := {
      val log = streams.value.log
      val generatedDir = generateClassGraphEntry.value
      val entryClassesDir = target.value / "entry-classes"
      IO.createDirectory(entryClassesDir)

      val kotlinFilesDir = generatedDir / "main" / "kotlin"
      val kotlinFiles = if (kotlinFilesDir.exists()) {
        (kotlinFilesDir ** "*.kt").get
      } else {
        Seq.empty
      }

      // Fix enum codegen bug in generated Kotlin files
      kotlinFiles.foreach { file =>
        val content = IO.read(file)
        // Replace incorrect VariantCasterENUM<EnumType> syntax with correct VariantCaster.ENUM(enumValues<EnumType>())
        val fixedContent = content.replaceAll(
          """VariantCasterENUM<(\w+)>""",
          """godot.core.VariantCaster.ENUM(enumValues<$1>())"""
        )
        if (content != fixedContent) {
          log.info(s"[Entry] Fixed enum syntax in ${file.getName}")
          IO.write(file, fixedContent)
        }
      }

      val providedJars = Classpaths.managedJars(Provided, classpathTypes.value, update.value).files
      val fullClasspathFiles = (Compile / fullClasspath).value.files

      if (kotlinFiles.nonEmpty) {
        val cp = (fullClasspathFiles ++ providedJars).mkString(":")
        val kotlincCp = providedJars.mkString(":")

        try {
          val args = Seq(
            "java",
            "-cp",
            kotlincCp,
            "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler",
            "-d",
            entryClassesDir.getAbsolutePath,
            "-cp",
            cp,
            "-jvm-target",
            "17"
          ) ++ kotlinFiles.map(_.getAbsolutePath)
          Process(args).!
        } catch {
          case e: Exception => log.warn(s"[Entry] Warning: ${e.getMessage}")
        }
      }

      entryClassesDir
    },
    packageBootstrapJar := {
      val log = streams.value.log
      val destJar = target.value / "godot-bootstrap.jar"
      log.info("[Godot] Packaging bootstrap JAR...")

      val bootstrapDeps = Set(
        "kotlin-stdlib",
        "kotlin-reflect",
        "godot-internal-library-debug",
        "godot-core-library-debug",
        "godot-api-library-debug",
        "godot-bootstrap-library-debug",
        "godot-extension-library-debug",
        "godot-build-props",
        "common"
      )

      val jars = Classpaths.managedJars(Provided, classpathTypes.value, update.value).files
        .filter(f => bootstrapDeps.exists(dep => f.getName.contains(dep)))

      val jarDir = target.value / "bootstrap-contents"
      if (jarDir.exists()) IO.delete(jarDir)
      IO.createDirectory(jarDir)

      log.info(s"[Godot] Extracting ${jars.size} bootstrap JARs...")
      jars.foreach { jar =>
        Process(Seq("jar", "xf", jar.getAbsolutePath), jarDir).!
      }

      val metaInf = jarDir / "META-INF"
      if (metaInf.exists()) {
        IO.listFiles(metaInf).filter(f =>
          f.getName.endsWith(".SF") || f.getName.endsWith(".DSA") || f.getName.endsWith(".RSA")
        )
          .foreach(IO.delete)
      }

      Process(Seq("jar", "cf", destJar.getAbsolutePath, "-C", jarDir.getAbsolutePath, ".")).!
      log.info(s"[Godot] Bootstrap JAR created: $destJar")
      destJar
    },
    packageMainJar := {
      val log = streams.value.log
      val compiledClasses = (Compile / classDirectory).value
      val destJar = target.value / "main.jar"
      log.info("[Godot] Packaging main JAR...")

      val jarDir = target.value / "jar-contents"
      if (jarDir.exists()) IO.delete(jarDir)
      IO.createDirectory(jarDir)

      // Copy compiled classes from current module
      if (compiledClasses.exists() && compiledClasses.isDirectory) {
        IO.copyDirectory(compiledClasses, jarDir)
      }

      // Copy compiled classes from all dependent modules
      val dependentClassDirs = (Compile / dependencyClasspath).value.files.filter { f =>
        f.exists() && f.isDirectory && f.getPath.contains("/target/scala-")
      }

      dependentClassDirs.foreach { classDir =>
        if (classDir.exists() && classDir.isDirectory) {
          log.info(s"[Godot] Including classes from module: ${classDir.getPath}")
          IO.copyDirectory(classDir, jarDir)
        }
      }

      // Copy entry classes
      val entryClasses = compileClassGraphEntry.value
      if (entryClasses.exists() && entryClasses.listFiles.nonEmpty) {
        IO.copyDirectory(entryClasses, jarDir)
      }

      // Copy resources
      val resourcesDir = (Compile / resourceDirectory).value
      if (resourcesDir.exists() && resourcesDir.isDirectory) {
        IO.copyDirectory(resourcesDir, jarDir)
      }

      // Extract non-bootstrap dependencies
      val excludePatterns = Set(
        "kotlin-stdlib",
        "kotlin-reflect",
        "godot-library",
        "godot-api-library",
        "godot-core-library",
        "godot-bootstrap-library",
        "godot-extension-library",
        "godot-internal-library",
        "godot-coroutine-library",
        "godot-build-props",
        "common"
      )

      val depsToExtract = (Compile / fullClasspath).value.files.filter { f =>
        f.exists() && f.getName.endsWith(".jar") && !excludePatterns.exists(pattern => f.getName.contains(pattern))
      }

      if (depsToExtract.nonEmpty) {
        log.info(s"[Godot] Extracting ${depsToExtract.size} dependency JARs...")
        depsToExtract.foreach { jar =>
          Process(Seq("jar", "xf", jar.getAbsolutePath), jarDir).!
        }
      }

      // Clean up META-INF signatures
      val metaInf = jarDir / "META-INF"
      if (metaInf.exists()) {
        IO.listFiles(metaInf).filter(f =>
          f.getName.endsWith(".SF") || f.getName.endsWith(".DSA") || f.getName.endsWith(".RSA")
        )
          .foreach(IO.delete)
      }

      Process(Seq("jar", "cf", destJar.getAbsolutePath, "-C", jarDir.getAbsolutePath, ".")).!
      log.info(s"[Godot] Main JAR created: $destJar")
      destJar
    },
    generateGdIgnoreFiles :=
      Vector(
        baseDirectory.value / "target",
        baseDirectory.value / "modules",
        baseDirectory.value / "src",
        baseDirectory.value / "jvm"
      ).filter(d => !d.exists()).foreach { dir =>
        IO.write(dir / ".gdignore", "")
      }
  )
}
