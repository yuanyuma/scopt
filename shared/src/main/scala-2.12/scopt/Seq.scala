package scopt

import scala.annotation.compileTimeOnly

/**
  * In Scala 2.13, scala.Seq moved from scala.collection.Seq to scala.collection.immutable.Seq.
  * In this code base, we'll require you to name ISeq or CSeq.
  *
  * import scala.collection.{ Seq => CSeq }
  * import scala.collection.immutable.{ Seq => ISeq }
  *
  * This Seq trait is a dummy type to prevent the use of `Seq`.
  */
@compileTimeOnly("Use ISeq or CSeq") private[scopt] trait Seq[A1, F1[A2], A3]
