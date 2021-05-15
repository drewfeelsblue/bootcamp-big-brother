package http

import io.estatico.newtype.macros.newtype

object Model {
  @newtype final case class Topic(value: String)
  @newtype final case class Exercise(value: String)
}
