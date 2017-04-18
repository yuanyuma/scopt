package scopt

import scala.scalajs.js
import scala.scalajs.js.annotation._

// js version

@js.native
@JSImport("os", JSImport.Namespace)
object OS extends js.Object {
  val EOL: String = js.native
}

class ParseException(s: String, errorOffset: Int) extends Exception(s)

private[scopt] object platform {
  val _NL = OS.EOL

  type ParseException = scopt.ParseException
  def mkParseEx(s: String, p: Int) = new ParseException(s, p)

  trait Implicits {
  }
}
