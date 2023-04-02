import org.jetbrains.sbtidea.{AutoJbr, JbrPlatform}

lazy val scala213           = "2.13.10"
lazy val scalaPluginVersion = "2023.1.13"
lazy val pluginVersion      = "2023.1.23.1"

ThisBuild / intellijPluginName := "intellij-airspec"
ThisBuild / intellijBuild := "223.8836.41"
ThisBuild / jbrInfo := AutoJbr(explicitPlatform = Some(JbrPlatform.osx_aarch64))

Global / intellijAttachSources := true

lazy val globalJavacOptionsCommon = Seq(
  "-Xlint:unchecked"
)
lazy val globalScalacOptionsCommon = Seq(
  "-explaintypes",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint:serial",
  "-Xfatal-warnings",
  "-language:existentials",
  "-Ytasty-reader",
)

// options for modules which classes can only be used in IDEA process (uses JRE 11)
lazy val globalJavacOptions: Seq[String] = globalJavacOptionsCommon ++ Seq("--release", "17")
lazy val globalScalacOptions: Seq[String] = globalScalacOptionsCommon //++ Seq("--release", "17")

// options for modules which classes can be used outside IDEA process with arbitrary JVM version, e.g.:
//  - in JPS process (JDK is calculated based on project & module JDK)
//  - in Compile server (by default used project JDK version, can be explicitly changed by user)
lazy val outOfIDEAProcessJavacOptions: Seq[String] = globalJavacOptionsCommon ++ Seq("--release", "8")
lazy val outOfIDEAProcessScalacOptions: Seq[String] = globalScalacOptionsCommon //++ Seq("--release", "8")

//(Global / javacOptions) := Seq("--release", "17")

//ThisBuild / scalacOptions ++= Seq(
//  "-explaintypes",
//  "-deprecation",
//  "-unchecked",
//  "-feature",
//  "-Xlint:serial",
//  "-Ymacro-annotations",
//  "-Xfatal-warnings",
//  "-language:implicitConversions",
//  "-language:reflectiveCalls",
//  "-language:existentials"
//)

lazy val root =
  newProject("intellij-airspec", file("."))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      patchPluginXml := pluginXmlOptions { xml =>
        xml.version = version.value
      }
    )
    .dependsOn(runner % "test->test;compile->compile")

lazy val runner: Project =
  newProject("runners", file("runner"))
    .settings(
      (Compile / javacOptions) := outOfIDEAProcessJavacOptions,
      (Compile / scalacOptions) := outOfIDEAProcessScalacOptions,
      packageMethod := PackagingMethod.Standalone(static = true),
      libraryDependencies += "org.wvlet.airframe" %% "airspec" % "23.3.4" % "provided"
    )

def newProject(projectName: String, base: File): Project =
  Project(projectName, base).settings(
    name := projectName,
    scalaVersion := scala213,
    version := pluginVersion,
    (Compile / javacOptions) := globalJavacOptions,
    (Compile / scalacOptions) := globalScalacOptions,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-s", "-a", "+c", "+q"),
    intellijPlugins := Seq(
      "com.intellij.java".toPlugin,
      s"org.intellij.scala:$scalaPluginVersion".toPlugin
    ),
    (Test / scalacOptions) += "-Xmacro-settings:enable-expression-tracers"
  )