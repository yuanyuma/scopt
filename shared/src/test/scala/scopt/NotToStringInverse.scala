/**
 * The important thing about this config option is that `reads` not be able to parse the
 * result of `NotToStringInverse#toString`.
 */
case class NotToStringInverse(s0: String, len: Int)

object NotToStringInverse {
  val empty: NotToStringInverse = NotToStringInverse("", 0)
  implicit val reads: scopt.Read[NotToStringInverse] =
    new scopt.Read[NotToStringInverse] {
      override val arity: Int = 1

      override val reads: String => NotToStringInverse =
        (s: String) => (NotToStringInverse(s, s.length))
    }
}

case class ConfigWithNotToStringInverse(x: NotToStringInverse)

object ConfigWithNotToStringInverse {
  val empty: ConfigWithNotToStringInverse = ConfigWithNotToStringInverse(NotToStringInverse.empty)
}
