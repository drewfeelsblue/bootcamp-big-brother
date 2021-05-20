package modules

import cats.effect.{Resource, Sync}
import service.{ResponseService, TaskService, TokenService}
import skunk.Session

sealed abstract case class Services[F[_]] private (
  tokens: TokenService[F],
  tasks: TaskService[F],
  responses: ResponseService[F]
)

object Services {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): Services[F] =
    new Services[F](TokenService.make(sessionPool), TaskService.make(sessionPool), ResponseService.make(sessionPool)) {}
}
