package scopt


private[scopt] object platform {
  val _NL = System.getProperty("line.separator")

  import java.util.{Locale, Calendar, GregorianCalendar}
  import java.text.SimpleDateFormat
  import java.io.File
  import java.net.InetAddress

  type ParseException = java.text.ParseException
  def mkParseEx(s: String, p: Int) = new java.text.ParseException(s, p)

  trait Implicits {
    def calendarRead(pattern: String): Read[Calendar] = calendarRead(pattern, Locale.getDefault)
    def calendarRead(pattern: String, locale: Locale): Read[Calendar] =
      Read.reads { s =>
        val fmt = new SimpleDateFormat(pattern)
        val c = new GregorianCalendar
        c.setTime(fmt.parse(s))
        c
      }

    implicit val yyyymmdddRead: Read[Calendar] = calendarRead("yyyy-MM-dd")
    implicit val fileRead: Read[File]           = Read.reads { new File(_) }
    implicit val inetAddress: Read[InetAddress] = Read.reads { InetAddress.getByName(_) }
  }
}
