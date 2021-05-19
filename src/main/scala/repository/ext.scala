package repository

import scala.language.implicitConversions
import io.estatico.newtype.Coercible
import skunk.Codec

object ext {
  implicit def newTypeCodec[A, B](codec: Codec[A])(implicit atob: Coercible[A, B], btoa: Coercible[B, A]): Codec[B] =
    codec.imap(atob.apply)(btoa.apply)
}
