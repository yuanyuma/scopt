import SpecUtilPlatform._

object SpecUtil {
  implicit class RichString(self: String) {
    def newline: String = self + envLineSeparator
    def newlines: String = self.lines.mkString(envLineSeparator)
  }
}
