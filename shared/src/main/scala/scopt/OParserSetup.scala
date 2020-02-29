package scopt

trait OParserSetup {
  def renderingMode: RenderingMode
  def errorOnUnknownArgument: Boolean

  /**
   * Show usage text on parse error.
   * Defaults to None, which displays the usage text if
   * --help option is not defined.
   */
  def showUsageOnError: Option[Boolean]
  def displayToOut(msg: String): Unit
  def displayToErr(msg: String): Unit
  def reportError(msg: String): Unit
  def reportWarning(msg: String): Unit
  def terminate(exitState: Either[String, Unit]): Unit
}

abstract class DefaultOParserSetup extends OParserSetup {
  override def renderingMode: RenderingMode = RenderingMode.TwoColumns
  override def errorOnUnknownArgument: Boolean = true
  override def showUsageOnError: Option[Boolean] = None
  override def displayToOut(msg: String): Unit = {
    Console.out.println(msg)
  }
  override def displayToErr(msg: String): Unit = {
    Console.err.println(msg)
  }
  override def reportError(msg: String): Unit = {
    displayToErr("Error: " + msg)
  }
  override def reportWarning(msg: String): Unit = {
    displayToErr("Warning: " + msg)
  }
  override def terminate(exitState: Either[String, Unit]): Unit =
    exitState match {
      case Left(_)  => platform.exit(1)
      case Right(_) => platform.exit(0)
    }
}
