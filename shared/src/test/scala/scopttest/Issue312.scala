package scopttest

import scopt.OptionParser

case class Issue312Config(args: Seq[String])

object Issue312 extends verify.BasicTestSuite /*munit.FunSuite*/ {
  val parser = new OptionParser[Issue312Config]("test-args") {
    arg[String]("[source_path...] <target_path>")
      .minOccurs(2)
      .unbounded()
      .action((u, c) => c.copy(args = c.args :+ u))
  }

  val parserMore = new OptionParser[Issue312Config]("test-args") {
    arg[String]("[source_path...] <target_path>")
      .minOccurs(4)
      .unbounded()
      .action((u, c) => c.copy(args = c.args :+ u))
  }

  val parserNoMin = new OptionParser[Issue312Config]("test-args") {
    arg[String]("[source_path...] <target_path>")
      .unbounded()
      .action((u, c) => c.copy(args = c.args :+ u))
  }

  test("minOccurs works correctly") {
    val parsed = parser.parse(Array("1", "2"), Issue312Config(Seq.empty))

    assert(parsed.contains(Issue312Config(Seq("1", "2"))))
  }

  test("larger setting works as expected") {
    val data = Seq(1, 2, 3, 4, 5).map(_.toString)

    val parsed = parserMore.parse(data, Issue312Config(Seq.empty))

    assert(parsed.contains(Issue312Config(data)))
  }

  test("fails without enough args") {
    val data = Seq(1).map(_.toString)

    val parsed = parserMore.parse(data, Issue312Config(Seq.empty))

    assert(parsed.isEmpty)
  }

  test("fails nearly enough args") {
    val data = Seq(1, 2, 3).map(_.toString)

    val parsed = parserMore.parse(data, Issue312Config(Seq.empty))

    assert(parsed.isEmpty)
  }

  test("minOccurs change does not break default behaviour") {
    val data = Seq(1).map(_.toString)
    val bigData = Seq(1, 2, 3).map(_.toString)

    val parsed = parserNoMin.parse(data, Issue312Config(Seq.empty))

    assert(parsed.contains(Issue312Config(data)))

    val parsedBig = parserNoMin.parse(bigData, Issue312Config(Seq.empty))

    assert(parsedBig.contains(Issue312Config(bigData)))
  }
}
