package scopttest

import minitest._
import scala.concurrent.duration.Duration
import scopt.OParser
import SpecUtil._

object MonadicParserSpec extends SimpleTestSuite with PowerAssertions {
  val NL = System.getProperty("line.separator")

  test("programName(s) should generate usage text") {
    val builder = OParser.builder[Config]
    val programName1: OParser[Unit, Config] = {
      import builder._
      programName("scopt")
    }
    assert(
      OParser.usage(programName1) ==
        """Usage: scopt
        |
        |""".stripMargin)
    ()
  }

  test("head(s, ...) should generate usage text") {
    assert(
      OParser.usage(head1) ==
        """scopt 3.x
        |""".stripMargin)
    ()
  }

  test("head(s, ...) should compose using ++") {
    val p = head1 ++ head2
    assert(
      OParser.usage(p) ==
        """scopt 3.x
        |x y
        |""".stripMargin)
    ()
  }

  test("head(s, ...) should compose using for comprehension") {
    val builder = OParser.builder[Config]
    val programName1: OParser[Unit, Config] = {
      import builder._
      programName("scopt")
    }
    val p: OParser[Unit, Config] = for {
      _ <- programName1
      _ <- head1
    } yield ()
    assert(
      OParser.usage(p) ==
        """scopt 3.x
        |Usage: scopt
        |
        |""".stripMargin)
    ()
  }

  test("unit parser should generate usage") {
    val builder = OParser.builder[Config]
    val unitParser1: OParser[Unit, Config] = {
      import builder._
      for {
        _ <- programName("scopt")
        _ <- head("scopt", "3.x")
        _ <- opt[Unit]('f', "foo").action((x, c: Config) => c.copy(flag = true))
      } yield ()
    }
    assert(
      OParser.usage(unitParser1) ==
        """scopt 3.x
        |Usage: scopt [options]
        |
        |  -f, --foo  """.stripMargin)
    ()
  }

  test("unit parser should parse ()") {
    val builder = OParser.builder[Config]
    val unitParser1: OParser[Unit, Config] = {
      import builder._
      for {
        _ <- programName("scopt")
        _ <- head("scopt", "3.x")
        _ <- opt[Unit]('f', "foo") action { (x, c: Config) =>
          c.copy(flag = true)
        }
      } yield ()
    }

    def unitParser(args: String*): Unit = {
      val result = OParser.parse(unitParser1, args.toSeq, Config())
      assert(result.get.flag == true)
    }
    unitParser("--foo")
    unitParser("-f")
  }

  test("""for {
    |  _ <- opt[Unit]('a', "alice")
    |  _ <- opt[Unit]('b', "bob")
    |  _ <- opt[Unit]("alicebob") abbr("ab") action { x => x }
    |} yield ()""".stripMargin + " should generate usage") {
    assert(
      OParser.usage(groupParser1) ==
        """scopt 3.x
        |Usage: scopt [options]
        |
        |  -a, --alice      
        |  -b, --bob        
        |  -ab, --alicebob  """.stripMargin)
    ()
  }

  test("grouped parser should parse ()") {
    groupParser("-ab")
    groupParser("-abab")
  }

  test("int parser should generate usage") {
    assert(
      OParser.usage(intParser1) ==
        """scopt 3.x
        |Usage: scopt [options]
        |
        |  -f, --foo <value>  
        |  --help             """.stripMargin)
    ()
  }

  test("int parser should parse 1") {
    intParser("--foo", "1")
    intParser("--foo:1")
    intParser("--foo=1")
    intParser("-f", "1")
    intParser("-f:1")
    intParser("-f=1")
    intParser("--foo", "0x01")
    intParser("--foo:0x01")
    intParser("--foo=0x01")
    intParser("-f", "0x1")
    intParser("-f:0x1")
    intParserFail { "--foo" }
    intParserFail("--foo", "bar")
    intParserFail("--foo=bar")
  }

  test("BigDecimal parser should generate usage") {
    assert(
      OParser.usage(bigDecimalParser1) ==
        """scopt 3.x
        |Usage: scopt [options]
        |
        |  -f, --foo <value>  
        |  --help             """.stripMargin)
    ()
  }

  test("BigDecimal parser should parse 1.0") {
    bigDecimalParser("--foo", "1.0")
    bigDecimalParser("--foo=1.0")
    bigDecimalParserFail("--foo", "bar")
    bigDecimalParserFail("--foo=bar")
  }

  test("""opt[String]("foo").required() action { x => x }""" + " should fail to parse Nil") {
    val builder = OParser.builder[Config]
    val parser: OParser[Unit, Config] = {
      import builder._
      for {
        _ <- programName("scopt")
        _ <- head("scopt", "3.x")
        _ <- opt[String]("foo").required().action((x, c) => c.copy(stringValue = x))
        _ <- help("help")
      } yield ()
    }
    val result = OParser.parse(parser, Nil, Config())
    assert(result == None)
    ()
  }

  test("custom validation") {
    val builder = OParser.builder[Config]
    val parser: OParser[Unit, Config] = {
      import builder._
      for {
        _ <- programName("scopt")
        _ <- head("scopt", "3.x")
        _ <- opt[Int]('f', "foo")
          .action((x, c) => c.copy(intValue = x))
          .validate(x =>
            if (x > 0) success
            else failure("Option --foo must be >0"))
          .validate(x => failure("Just because"))
      } yield ()
    }
    val result = OParser.parse(parser, List("--foo", "0"), Config())
    assert(result == None)
    ()
  }

  test("command usage") {
    assert(
      OParser.usage(cmdParser1) ==
        """scopt 3.x
        |Usage: scopt [update]
        |
        |Command: update [options]
        |
        |  --foo                    
        |  --bar <value>            """.stripMargin)
    ()
  }

  test("option parser can be reused across multiple commands") {
    assert(
      OParser.usage(cmdParser2) ==
        """scopt 3.x
        |Usage: scopt [update|status]
        |
        |Command: update [options]
        |
        |  --foo                    
        |  --bar <value>            
        |
        |Command: status [options]
        |
        |  --foo                    
        |  --bar <value>            
        |""".stripMargin)
    ()
  }

  test("--version should display header") {
    val builder = OParser.builder[Config]
    val parser: OParser[Unit, Config] = {
      import builder._
      for {
        _ <- programName("scopt")
        _ <- head("scopt", "3.x")
        _ <- version("version")
      } yield ()
    }
    val out = printParserOut {
      OParser.parse(parser, List("--version"), Config(), new scopt.DefaultOParserSetup {
        override def terminate(exitState: Either[String, Unit]): Unit = ()
      })
    }
    assert(out == "scopt 3.x".newline)
    ()
  }

  test("--help should display the usage text") {
    val builder = OParser.builder[Config]
    val parser: OParser[Unit, Config] = {
      import builder._
      for {
        _ <- programName("scopt")
        _ <- head("scopt", "3.x")
        _ <- help("help").text("prints this usage text")
      } yield ()
    }
    val out = printParserOut {
      OParser.parse(parser, List("--help"), Config(), new scopt.DefaultOParserSetup {
        override def terminate(exitState: Either[String, Unit]): Unit = ()
      })
    }
    assert(
      out ==
        """scopt 3.x
        |Usage: scopt [options]
        |
        |  --help  prints this usage text
        |""".stripMargin)
    ()
  }

  // examples
  val builder = OParser.builder[Config]

  lazy val head1: OParser[Unit, Config] = {
    import builder._
    head("scopt", "3.x")
  }

  lazy val head2: OParser[Unit, Config] = {
    import builder._
    head("x", "y")
  }

  lazy val groupParser1: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- programName("scopt")
      _ <- head("scopt", "3.x")
      _ <- opt[Unit]('a', "alice")
      _ <- opt[Unit]('b', "bob")
      _ <- opt[Unit]("alicebob").abbr("ab").action((x, c) => c.copy(flag = true))
      // _ <_ help("help")
    } yield ()
  }

  def groupParser(args: String*): Unit = {
    val result = OParser.parse(groupParser1, args.toSeq, Config())
    assert(result.get.flag)
  }

  lazy val intParser1: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- programName("scopt")
      _ <- head("scopt", "3.x")
      _ <- opt[Int]('f', "foo").action((x, c) => c.copy(intValue = x))
      _ <- help("help")
    } yield ()
  }

  def intParser(args: String*): Unit = {
    val result = OParser.parse(intParser1, args.toSeq, Config())
    assert(result.get.intValue == 1)
  }
  def intParserFail(args: String*): Unit = {
    val result = OParser.parse(intParser1, args.toSeq, Config())
    assert(result == None)
  }

  lazy val bigDecimalParser1: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- programName("scopt")
      _ <- head("scopt", "3.x")
      _ <- opt[BigDecimal]('f', "foo").action((x, c) => c.copy(bigDecimalValue = x))
      _ <- help("help")
    } yield ()
  }

  def bigDecimalParser(args: String*): Unit = {
    val result = OParser.parse(bigDecimalParser1, args.toSeq, Config())
    assert(result.get.bigDecimalValue == BigDecimal("1.0"))
  }

  def bigDecimalParserFail(args: String*): Unit = {
    val result = OParser.parse(bigDecimalParser1, args.toSeq, Config())
    assert(result == None)
  }

  lazy val checkParser1: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- programName("scopt")
      _ <- head("scopt", "3.x")
      _ <- opt[Unit]('f', "foo").action((x, c) => c.copy(flag = true))
      _ <- checkConfig { c =>
        if (c.flag) success else failure("flag is false")
      }
    } yield ()
  }

  lazy val cmdParser1: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- programName("scopt")
      _ <- head("scopt", "3.x")
      _ <- cmd("update")
        .action((x, c) => c.copy(flag = true))
        .children(for {
          _ <- opt[Unit]("foo").action((x, c) => c.copy(stringValue = "foo"))
          _ <- opt[Int]("bar").action((x, c) => c.copy(intValue = x))
        } yield ())
    } yield ()
  }

  lazy val suboptionParser1: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- opt[Unit]("foo").action((x, c) => c.copy(stringValue = "foo"))
      _ <- opt[Int]("bar").action((x, c) => c.copy(intValue = x))
      _ <- note("")
    } yield ()
  }

  lazy val cmdParser2: OParser[Unit, Config] = {
    import builder._
    for {
      _ <- programName("scopt")
      _ <- head("scopt", "3.x")
      _ <- cmd("update")
        .action((x, c) => c.copy(flag = true))
        .children(suboptionParser1)
      _ <- cmd("status")
        .action((x, c) => c.copy(flag = true))
        .children(suboptionParser1)
    } yield ()
  }

  case class Config(
      flag: Boolean = false,
      intValue: Int = 0,
      longValue: Long = 0L,
      stringValue: String = "",
      doubleValue: Double = 0.0,
      boolValue: Boolean = false,
      debug: Boolean = false,
      bigDecimalValue: BigDecimal = BigDecimal("0.0"),
      durationValue: Duration = Duration("0s"),
      key: String = "",
      a: String = "",
      b: String = "",
      seqInts: Seq[Int] = Seq(),
      mapStringToBool: Map[String, Boolean] = Map(),
      seqTupleStringString: Seq[(String, String)] = Nil,
      charValue: Char = 0)
}
