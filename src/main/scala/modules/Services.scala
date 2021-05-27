package modules

import cats.Parallel
import cats.effect.{Concurrent, Resource, Timer}
import cats.syntax.functor._
import service.{ResponseService, TaskService, TokenService}
import skunk.Session
import com.evolutiongaming.catshelper.Runtime
import org.typelevel.log4cats.Logger

sealed abstract case class Services[F[_]] private (
  tokens: TokenService[F],
  tasks: TaskService[F],
  responses: ResponseService[F]
)

object Services {
  def make[F[_]: Concurrent: Timer: Parallel: Runtime: Logger](sessionPool: Resource[F, Session[F]]): F[Services[F]] =
    TokenService
      .make(sessionPool)
      .map(new Services[F](_, TaskService.make(sessionPool), ResponseService.make(sessionPool)) {})
}
