package scopttest

import SpecUtilPlatform._
import java.io.ByteArrayOutputStream

object SpecUtil {
  implicit class RichString(self: String) {
    def newline: String = self + envLineSeparator
    def newlines: String =
      // Predef.augmentString = work around scala/bug#11125 on JDK 11
      augmentString(self).lines.mkString(envLineSeparator)

    def linesList: List[String] =
      augmentString(self).lines.toList
  }

  def printParserOut(thunk: => Unit): String = {
    val outStream = new ByteArrayOutputStream()
    Console.withOut(outStream) { thunk }
    outStream.toString("UTF-8")
  }

  def printParserError(thunk: => Unit): String = {
    val errStream = new ByteArrayOutputStream()
    Console.withErr(errStream) { thunk }
    errStream.toString("UTF-8")
  }
}
