name := "scopt"

version := "3.3.0"

organization := "com.github.scopt"

homepage := Some(url("https://github.com/scopt/scopt"))

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

description := """a command line options parsing library"""

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.11.4", "2.10.4", "2.9.1", "2.9.2", "2.9.3")

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
  val testVersion = sv match {
    case "2.9.3" => "1.12.5-SNAPSHOT"
    case "2.9.2" => "1.12.3"
    case "2.9.1" => "1.12.4"
    case "2.10.4" => "2.3.3"
    case x if x startsWith "2.11" => "2.3.11"
    case _ => error("Unsupported Scala version " + sv)
  }
  deps :+ ("org.specs2" %% "specs2" % testVersion % "test")
}

resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public"

// scaladoc fix
unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))
