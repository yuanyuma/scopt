package scopt

import scala.collection.{ Seq => CSeq }

abstract class OParserBuilder[C] {
  def programName(x: String): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.ProgramName, "")).text(x)

  /** adds usage text. */
  def head(xs: String*): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.Head, "")).text(xs.mkString(" "))

  /** adds an option invoked by `--name x`.
   * @param name name of the option
   */
  def opt[A: Read](name: String): OParser[A, C] =
    wrap(makeDef[A](OptionDefKind.Opt, name))

  /** adds an option invoked by `-x value` or `--name value`.
   * @param x name of the short option
   * @param name name of the option
   */
  def opt[A: Read](x: Char, name: String): OParser[A, C] =
    opt[A](name).abbr(x.toString)

  /** adds usage text. */
  def note(x: String): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.Note, "").text(x))

  /** adds an argument invoked by an option without `-` or `--`.
   * @param name name in the usage text
   */
  def arg[A: Read](name: String): OParser[A, C] =
    wrap(makeDef(OptionDefKind.Arg, name)).required()

  /** adds a command invoked by an option without `-` or `--`.
   * @param name name of the command
   */
  def cmd(name: String): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.Cmd, name))

  /** adds final check. */
  def checkConfig(f: C => Either[String, Unit]): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.Check, "").validateConfig(f))

  /** adds an option invoked by `--name` that displays header text and exits.
   * @param name name of the option
   */
  def version(name: String): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.OptVersion, name))

  /** adds an option invoked by `-x` or `--name` that displays header text and exits.
   * @param x name of the short option
   * @param name name of the option
   */
  def version(x: Char, name: String): OParser[Unit, C] =
    version(name).abbr(x.toString)

  /** adds an option invoked by `--name` that displays usage text and exits.
   * @param name name of the option
   */
  def help(name: String): OParser[Unit, C] =
    wrap(makeDef[Unit](OptionDefKind.OptHelp, name))

  /** adds an option invoked by `-x` or `--name` that displays usage text and exits.
   * @param x name of the short option
   * @param name name of the option
   */
  def help(x: Char, name: String): OParser[Unit, C] =
    help(name).abbr(x.toString)

  /** call this to express success in custom validation. */
  def success: Either[String, Unit] = OptionDef.makeSuccess[String]

  /** call this to express failure in custom validation. */
  def failure(msg: String): Either[String, Unit] = Left(msg)

  protected def wrap[A](d: OptionDef[A, C]): OParser[A, C] =
    OParser(d, Nil)

  protected def makeDef[A: Read](kind: OptionDefKind, name: String): OptionDef[A, C] =
    new OptionDef[A, C](kind, name)
}

/**
 * A monadic commandline options parser.
 */
class OParser[A, C](head: OptionDef[A, C], rest: List[OptionDef[_, C]]) {

  /** Adds description in the usage text. */
  def text(x: String): OParser[A, C] = subHead[A](head.text(x))

  /** Adds short option -x. */
  def abbr(x: String): OParser[A, C] = subHead[A](head.abbr(x))

  /** Adds a callback function. */
  def action(f: (A, C) => C): OParser[A, C] = subHead[A](head.action(f))

  /** Requires the option to appear at least `n` times. */
  def minOccurs(n: Int): OParser[A, C] = subHead[A](head.minOccurs(n))

  /** Allows the argument to appear at most `n` times. */
  def maxOccurs(n: Int): OParser[A, C] = subHead[A](head.maxOccurs(n))

  /** Requires the option to appear at least once. */
  def required(): OParser[A, C] = minOccurs(1)

  /** Chanages the option to be optional. */
  def optional(): OParser[A, C] = minOccurs(0)

  /** Allows the argument to appear multiple times. */
  def unbounded(): OParser[A, C] = maxOccurs(OptionDef.UNBOUNDED)

  /** Hides the option in any usage text. */
  def hidden(): OParser[A, C] = subHead[A](head.hidden())

  /** Adds value name used in the usage text. */
  def valueName(x: String): OParser[A, C] =
    subHead[A](head.valueName(x))

  /** Adds key name used in the usage text. */
  def keyName(x: String): OParser[A, C] =
    subHead[A](head.keyName(x))

  /** Adds key and value names used in the usage text. */
  def keyValueName(k: String, v: String): OParser[A, C] =
    subHead[A](head.keyValueName(k, v))

  /** Adds a parser under this command. */
  def children(cs: OParser[_, C]*): OParser[A, C] = {
    cs.toList match {
      case List() => this
      case List(c) =>
        val childList = c.toList
        val childListModified = c.toList map { _.parent(head) }
        OParser(head, rest ::: childListModified)
      case x :: xs =>
        children(OParser.sequence(x, xs: _*))
    }
  }

  /** Adds custom validation. */
  def validate(f: A => Either[String, Unit]): OParser[A, C] =
    subHead[A](head.validate(f))

  def toList: List[OptionDef[_, C]] = head :: rest
  def ++(other: OParser[_, C]): OParser[A, C] =
    OParser(head, rest ::: other.toList)

  def foreach(f: Unit => Unit): Unit = f(())

  def map(f: Unit => Unit): OParser[A, C] = this

  def flatMap(f: Unit => OParser[_, C]): OParser[A, C] =
    OParser(head, rest ::: f(()).toList)

  protected def subHead[B](head: OptionDef[B, C]): OParser[B, C] =
    OParser(head, rest)
}

object OParser {
  def apply[A, C](head: OptionDef[A, C], rest: List[OptionDef[_, C]]): OParser[A, C] =
    new OParser(head, rest)

  def builder[C]: OParserBuilder[C] = new OParserBuilder[C] {}

  def usage[C](parser: OParser[_, C]): String = usage(parser, RenderingMode.TwoColumns)

  def usage[C](parser: OParser[_, C], mode: RenderingMode): String = {
    val (h, u) = ORunner.renderUsage[C](mode, parser.toList)
    u
  }

  def sequence[A, C](parser: OParser[A, C], parsers: OParser[_, C]*): OParser[A, C] =
    if (parsers.isEmpty) parser
    else
      parser flatMap { p =>
        val ps = parsers.toList
        sequence(ps.head, ps.tail: _*)
      }

  private[this] lazy val setup = new DefaultOParserSetup with OParserSetup {
    def showUsageAsError(): Unit = ()
    def showTryHelp(): Unit = ()
  }

  def parse[C](parser: OParser[_, C], args: CSeq[String], init: C): Option[C] =
    ORunner.parse(args, init, parser.toList, setup)

  def parse[C](parser: OParser[_, C], args: CSeq[String], init: C, setup: OParserSetup): Option[C] =
    ORunner.parse(args, init, parser.toList, setup)
}
