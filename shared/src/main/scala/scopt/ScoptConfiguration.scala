package scopt

abstract class ScoptConfiguration {
  def errorOnUnknownArgument: Boolean
  def showUsageOnError: Boolean
  def reportError(msg: String): Unit
  def reportWarning(msg: String): Unit
  def showUsageAsError(): Unit
  def showTryHelp(): Unit
}

abstract class DefaultScoptConfiguration {
  def errorOnUnknownArgument: Boolean = true
  def showUsageOnError: Boolean = true
  def reportError(msg: String): Unit = {
    Console.err.println("Error: " + msg)
  }
  def reportWarning(msg: String): Unit = {
    Console.err.println("Warning: " + msg)
  }
}
