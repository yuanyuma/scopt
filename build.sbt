import Dependencies._

// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

def v: String = "3.7.2-SNAPSHOT"

ThisBuild / version := v
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala211, scala210, scala212, scala213)

lazy val root = (project in file("."))
  .aggregate(scoptJS, scoptJVM, scoptNative)
  .settings(
    name := "scopt root",
    publish / skip := true
  )

lazy val scopt = (crossProject(JSPlatform, JVMPlatform, NativePlatform) in file("."))
  .settings(
    name := "scopt",
    // site
    // to preview, preview-site
    // to push, ghpages-push-site
    siteSubdirName in SiteScaladoc := "$v/api",
    git.remoteRepo := "git@github.com:scopt/scopt.git",
    scalacOptions ++= Seq("-language:existentials", "-Xfuture", "-deprecation"),
    resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public",
    // scaladoc fix
    unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist")),
    testFrameworks += new TestFramework("verify.runner.Framework"),
  )
  .platformsSettings(JVMPlatform, JSPlatform)(
    libraryDependencies ++= parserCombinators.value,
  )
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scopt/scopt/" + sys.process.Process("git rev-parse HEAD").lineStream_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
    sources in Test := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          // specs 4.x does not support scala 2.10
          // specs 3.x does not support scala-js
          Nil
        case _ =>
          (sources in Test).value
      }
    }
  )
  .nativeSettings(
    sources in Test := Nil, // TODO https://github.com/monix/minitest/issues/12
    scalaVersion := scala211,
    crossScalaVersions := Nil
  )

lazy val scoptJS = scopt.js
  .settings(
    libraryDependencies += "com.eed3si9n.verify" %%% "verify" % verifyVersion % Test,
  )

lazy val scoptJVM = scopt.jvm.enablePlugins(SiteScaladocPlugin)
  .settings(
    libraryDependencies += "com.eed3si9n.verify" %% "verify" % verifyVersion % Test,
  )

lazy val scoptNative = scopt.native

lazy val nativeTest = (project in file("nativeTest"))
  .enablePlugins(ScalaNativePlugin)
  .dependsOn(scoptNative)
  .settings(scalaVersion := scala211)
