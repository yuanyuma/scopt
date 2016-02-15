def v: String = "3.4.0-SNAPSHOT"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(Seq(
      version := v,
      organization := "com.github.scopt",
      scalaVersion := "2.11.7",
      crossScalaVersions := Seq("2.11.7", "2.10.6"),
      homepage := Some(url("https://github.com/scopt/scopt")),
      licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
    )),
    name := "scopt",
    // site
    // to preview, preview-site
    // to push, ghpages-push-site
    site.settings,
    site.includeScaladoc(s"$v/api"),
    ghpages.settings,
    git.remoteRepo := "git@github.com:scopt/scopt.git",
    description := """a command line options parsing library""",
    libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
      val testVersion = sv match {
        case x if x startsWith "2.10." => "2.3.3"
        case x if x startsWith "2.11." => "2.3.11"
        case _ => error("Unsupported Scala version " + sv)
      }
      deps :+ ("org.specs2" %% "specs2" % testVersion % "test")
    },
    resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public",
    // scaladoc fix
    unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))
  )
