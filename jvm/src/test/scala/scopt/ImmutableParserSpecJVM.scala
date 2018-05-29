import minitest._
import java.util.{Calendar, GregorianCalendar}
import java.io.{ByteArrayOutputStream, File}
import java.net.{ URI, InetAddress }
import scala.concurrent.duration.Duration

object ImmutableParserSpecJVM extends SimpleTestSuite with PowerAssertions {
  test("calendar parser should parse 2000-01-01") {
    calendarParser("--foo", "2000-01-01")
    calendarParser("--foo=2000-01-01")
    calendarParserFail("--foo", "bar")
    calendarParserFail("--foo=bar")
  }

  test("file parser should parse test.txt") {
    fileParser("--foo", "test.txt")
    fileParser("--foo=test.txt")
  }

  test("InetAddress parser should parse 8.8.8.8") {
    inetAddressParser("--foo", "8.8.8.8")
    inetAddressParser("--foo=8.8.8.8")
  }

  import SpecUtil._

  val calendarParser1 = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")
    opt[Calendar]("foo").action( (x, c) => c.copy(calendarValue = x) )
    help("help")
  }
  def calendarParser(args: String*): Unit = {
    val result = calendarParser1.parse(args.toSeq, Config())
    assert(result.get.calendarValue.getTime == new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime)
  }
  def calendarParserFail(args: String*): Unit = {
    val result = calendarParser1.parse(args.toSeq, Config())
    assert(result == None)
  }

  val fileParser1 = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")
    opt[File]("foo").action( (x, c) => c.copy(fileValue = x) )
    help("help")
  }
  def fileParser(args: String*): Unit = {
    val result = fileParser1.parse(args.toSeq, Config())
    assert(result.get.fileValue == new File("test.txt"))
  }

  val inetAddressParser1 = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")
    opt[InetAddress]("foo").action( (x, c) => c.copy(inetAddressValue = x) )
    help("help")
  }
  def inetAddressParser(args: String*): Unit = {
    val result = inetAddressParser1.parse(args.toSeq, Config())
    assert(result.get.inetAddressValue == InetAddress.getByName("8.8.8.8"))
  }

  case class Config(flag: Boolean = false, intValue: Int = 0, longValue: Long = 0L, stringValue: String = "",
    doubleValue: Double = 0.0, boolValue: Boolean = false, debug: Boolean = false,
    bigDecimalValue: BigDecimal = BigDecimal("0.0"),
    calendarValue: Calendar = new GregorianCalendar(1900, Calendar.JANUARY, 1),
    fileValue: File = new File("."),
    uriValue: URI = new URI("http://localhost"),
    inetAddressValue: InetAddress = InetAddress.getByName("0.0.0.0"),
    durationValue: Duration = Duration("0s"),
    key: String = "", a: String = "", b: String = "",
    seqInts: Seq[Int] = Seq(),
    mapStringToBool: Map[String,Boolean] = Map(),
    seqTupleStringString: Seq[(String, String)] = Nil, charValue: Char = 0)
}
