import cats.implicits.toFunctorOps

package object config {
  final case class Secret[A](value: A) extends AnyVal {
    override def toString: String = value.toString.toList.as("*").mkString
  }

  trait DetailedToString extends Product {
    override def toString: String =
      (0 until productArity).map(i => s"${productElementName(i)}: ${productElement(i)}").mkString(", ")
  }
}
