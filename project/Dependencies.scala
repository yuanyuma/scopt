import sbt._
import Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  lazy val scala210 = "2.10.7"
  lazy val scala211 = "2.11.12"
  lazy val scala212 = "2.12.7"
  lazy val scala213 = "2.13.0-M5"

  lazy val specs2 = Def.setting {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        // specs2 4.x does not support Scala 2.10
        Seq("org.specs2" %% "specs2-core" % "3.9.5" % "test")
      case _ =>
        Seq("org.specs2" %%% "specs2-core" % "4.3.5" % "test")
    }
  }

  lazy val parserCombinators = Def.setting {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10 | 13)) =>
        Nil
      case _ =>
        Seq("org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.0" % Test)
    }
  }
}
