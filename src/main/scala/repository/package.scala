import io.estatico.newtype.Coercible
import skunk.Codec
import scala.language.implicitConversions

package object repository {
  implicit def newTypeCodec[A, B](codec: Codec[A])(implicit atob: Coercible[A, B], btoa: Coercible[B, A]): Codec[B] =
    codec.imap(atob.apply)(btoa.apply)
}
