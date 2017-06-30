resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.7.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.2")

ivyXML :=
  <dependency org="org.eclipse.jetty.orbit" name="javax.servlet"
  rev="2.5.0.v201103041518">
    <artifact name="javax.servlet" type="orbit" ext="jar"/>
  </dependency>

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.17")
addSbtPlugin("org.scala-native" % "sbt-crossproject"         % "0.1.0")
addSbtPlugin("org.scala-native" % "sbt-scalajs-crossproject" % "0.1.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native"         % "0.3.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0-M1")
