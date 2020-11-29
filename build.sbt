import Dependencies._

// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.crossProject

def v: String = "4.0.1-SNAPSHOT"

ThisBuild / version := v
ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala211, scala212, scala213)
ThisBuild / scalafmtOnCompile := true

lazy val root = (project in file("."))
  .aggregate(scoptJS, scoptJVM, scoptNative)
  .settings(
    name := "scopt root",
    publish / skip := true,
    crossScalaVersions := Nil,
  )

lazy val scopt = (crossProject(JSPlatform, JVMPlatform, NativePlatform) in file("."))
  .settings(
    name := "scopt",
    // site
    // to preview, preview-site
    // to push, ghpages-push-site
    siteSubdirName in SiteScaladoc := s"$v/api",
    git.remoteRepo := "git@github.com:scopt/scopt.git",
    scalacOptions ++= Seq("-language:existentials", "-deprecation"),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) =>
          Seq("-source:3.0-migration")
        case Some((2, v)) if v <= 12 =>
          Seq("-Xfuture")
        case _ =>
          Nil
      }
    },
    resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public",
    libraryDependencies += "com.eed3si9n.verify" %%% "verify" % verifyVersion % Test,
    testFrameworks += new TestFramework("verify.runner.Framework"),
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.19" % Test,
    testFrameworks += new TestFramework("munit.Framework"),
    // scaladoc fix
    unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))
  )
  .platformsSettings(JVMPlatform, JSPlatform)(
    Seq(Compile, Test).map { x =>
      unmanagedSourceDirectories in x += {
        baseDirectory.value.getParentFile / s"jvm_js/src/${Defaults.nameForSrc(x.name)}/scala/"
      }
    }
  )
  .jvmSettings(
    crossScalaVersions += scala3,
  )
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scopt/scopt/" + sys.process
        .Process("git rev-parse HEAD")
        .lineStream_!
        .head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
  )
  .nativeSettings(
    scalaVersion := scala211,
    crossScalaVersions := Nil
  )

lazy val scoptJS = scopt.js

lazy val scoptJVM = scopt.jvm
  .enablePlugins(SiteScaladocPlugin)
  .enablePlugins(GhpagesPlugin)

lazy val scoptNative = scopt.native
