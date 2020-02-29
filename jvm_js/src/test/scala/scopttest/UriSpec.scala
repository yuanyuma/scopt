package scopttest

import verify._
import java.net.URI
import SpecUtil._

object UriSpec extends BasicTestSuite {
  test("URI parser should parse an URI") {
    uriParser("--foo", "http://github.com/")
    uriParser("--foo=http://github.com/")
  }

  val uriParser1 = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")
    opt[URI]("foo").action((x, c) => c.copy(uriValue = x))
    help("help")
  }
  def uriParser(args: String*): Unit = {
    val result = uriParser1.parse(args.toSeq, Config())
    assert(result.get.uriValue == new URI("http://github.com/"))
  }
  case class Config(
      uriValue: URI = new URI("http://localhost")
  )
}
