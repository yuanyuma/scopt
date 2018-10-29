import SpecUtilPlatform._

object SpecUtil {
  implicit class RichString(self: String) {
    def newline: String = self + envLineSeparator
    def newlines: String =
      // Predef.augmentString = work around scala/bug#11125 on JDK 11
      augmentString(self).lines.mkString(envLineSeparator)
  }
}
