package service

import io.estatico.newtype.Coercible
import shapeless.ops.hlist.IsHCons
import shapeless.{Generic, HList, HNil}
import skunk.Codec

import scala.language.implicitConversions

package object queries {
  implicit def newTypeCodec[A, B](codec: Codec[A])(implicit atob: Coercible[A, B], btoa: Coercible[B, A]): Codec[B] =
    codec.imap(atob.apply)(btoa.apply)

  implicit def anyValCodec[A <: AnyVal, B, Repr <: HList](valCodec: Codec[B])(implicit
    gen: Generic.Aux[A, Repr],
    isHCons: IsHCons.Aux[Repr, B, HNil]
  ): Codec[A] = valCodec.imap[A](b => gen.from(isHCons.cons(b, HNil)))(a => isHCons.head(gen.to(a)))
}
