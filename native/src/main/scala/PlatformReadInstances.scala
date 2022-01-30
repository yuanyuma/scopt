package scopt

import java.io.File
import java.nio.file.{ Path, Paths }
import java.net.UnknownHostException
import collection.{ Seq => CSeq }

private[scopt] object platform {
  val _NL = System.getProperty("line.separator")

  type ParseException = Exception
  def mkParseEx(s: String, p: Int) = new Exception(s"$s at $p")

  trait PlatformReadInstances {
    implicit val fileRead: Read[File] = Read.reads { new File(_) }
    implicit val pathRead: Read[Path] = Read.reads { Paths.get(_) }
  }

  def applyArgumentExHandler[C](
      desc: String,
      arg: String
  ): PartialFunction[Throwable, Either[CSeq[String], C]] = {
    case e: NumberFormatException =>
      Left(List(desc + " expects a number but was given '" + arg + "'"))
    case e: Throwable => Left(List(desc + " failed when given '" + arg + "'. " + e.getMessage))
  }

  def exit(status: Int): Nothing = sys.exit(status)
}
