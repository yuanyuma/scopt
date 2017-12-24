import sbt._
import Keys._

object Dependencies {
  lazy val scala210 = "2.10.7"
  lazy val scala211 = "2.11.12"
  lazy val scala212 = "2.12.4"
  lazy val scala213 = "2.13.0-M2"

  lazy val specs2 = Def.setting {
    val v = CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        "3.9.5" // specs2 4.x does not support Scala 2.10
      case _ =>
        "4.0.2"
    }
    "org.specs2" %% "specs2-core" % v
  }
}
