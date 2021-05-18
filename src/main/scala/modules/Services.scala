package modules

import cats.effect.{ Resource, Sync }
import service.{ TaskService, TokenService }
import skunk.Session

final class Services[F[_]] private (
    val tokens: TokenService[F],
    val tasks: TaskService[F]
)

object Services {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): Services[F] =
    new Services[F](TokenService.make(sessionPool), TaskService.make(sessionPool))
}
