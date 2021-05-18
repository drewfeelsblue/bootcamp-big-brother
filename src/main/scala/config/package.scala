import cats.implicits.toFunctorOps
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{ Interval, Positive }
import eu.timepit.refined.string.IPv4

package object config {
  type Host              = String Refined IPv4
  type Port              = Int Refined Interval.Closed[0, 65353]
  type MaxSessionsNumber = Int Refined Positive

  final case class Secret[A](value: A) extends AnyVal {
    override def toString: String = value.toString.toList.as("*").mkString
  }

  trait DetailedToString extends Product {
    override def toString: String =
      (0 until productArity).map(i => s"${productElementName(i)}: ${productElement(i)}").mkString(", ")
  }
}
