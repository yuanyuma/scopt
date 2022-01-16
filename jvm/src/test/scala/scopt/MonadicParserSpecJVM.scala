package scopttest

import java.util.{ Calendar, GregorianCalendar }
import java.io.File
import scopt.OParser

object MonadicParserSpecJVM extends verify.BasicTestSuite {
  test("example parser should parse") {
    val result = OParser.parse(parser1, Array("--foo", "5", "--out", "target"), Config())
    assert(result == Some(Config(foo = 5, out = new File("target"))))
    ()
  }

  test("example parser generates usage text") {
    val expectedUsage = """scopt 4.x
Usage: scopt [update] [options] [<file>...]

  -f, --foo <value>        foo is an integer property
  -o, --out <file>         out is a required file property
  --max:<libname>=<max>    maximum count for <libname>
  -j, --jars <jar1>,<jar2>...
                           jars to include
  --kwargs k1=v1,k2=v2...  other arguments
  --verbose                verbose is a flag
  --help                   prints this usage text
  <file>...                optional unbounded args
some notes.

Command: update [options]
update is a command.
  -nk, --not-keepalive     disable keepalive
  --xyz <value>            xyz is a boolean property"""
    assert(OParser.usage(parser1) == expectedUsage)
    ()
  }

  lazy val builder = OParser.builder[Config]
  lazy val parser1: OParser[Unit, Config] = {
    import builder._
    OParser.sequence(
      programName("scopt"),
      head("scopt", "4.x"),
      opt[Int]('f', "foo")
        .action((x, c) => c.copy(foo = x))
        .text("foo is an integer property"),
      opt[File]('o', "out")
        .required()
        .valueName("<file>")
        .action((x, c) => c.copy(out = x))
        .text("out is a required file property"),
      opt[(String, Int)]("max")
        .action({ case ((k, v), c) => c.copy(libName = k, maxCount = v) })
        .validate(x =>
          if (x._2 > 0) success
          else failure("Value <max> must be >0")
        )
        .keyValueName("<libname>", "<max>")
        .text("maximum count for <libname>"),
      opt[Seq[File]]('j', "jars")
        .valueName("<jar1>,<jar2>...")
        .action((x, c) => c.copy(jars = x))
        .text("jars to include"),
      opt[Map[String, String]]("kwargs")
        .valueName("k1=v1,k2=v2...")
        .action((x, c) => c.copy(kwargs = x))
        .text("other arguments"),
      opt[Unit]("verbose")
        .action((_, c) => c.copy(verbose = true))
        .text("verbose is a flag"),
      opt[Unit]("debug")
        .hidden()
        .action((_, c) => c.copy(debug = true))
        .text("this option is hidden in the usage text"),
      help("help").text("prints this usage text"),
      arg[File]("<file>...")
        .unbounded()
        .optional()
        .action((x, c) => c.copy(files = c.files :+ x))
        .text("optional unbounded args"),
      note("some notes." + sys.props("line.separator")),
      cmd("update")
        .action((_, c) => c.copy(mode = "update"))
        .text("update is a command.")
        .children(
          opt[Unit]("not-keepalive")
            .abbr("nk")
            .action((_, c) => c.copy(keepalive = false))
            .text("disable keepalive"),
          opt[Boolean]("xyz")
            .action((x, c) => c.copy(xyz = x))
            .text("xyz is a boolean property"),
          opt[Unit]("debug-update")
            .hidden()
            .action((_, c) => c.copy(debug = true))
            .text("this option is hidden in the usage text"),
          checkConfig(c =>
            if (c.keepalive && c.xyz) failure("xyz cannot keep alive")
            else success
          )
        )
    )
  }

  case class Config(
      foo: Int = -1,
      out: File = new File("."),
      xyz: Boolean = false,
      libName: String = "",
      maxCount: Int = -1,
      verbose: Boolean = false,
      debug: Boolean = false,
      mode: String = "",
      files: Seq[File] = Seq(),
      keepalive: Boolean = false,
      jars: Seq[File] = Seq(),
      kwargs: Map[String, String] = Map()
  )
}
