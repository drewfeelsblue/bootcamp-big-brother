package http.routes

import cats.{ Defer, Monad, MonadError }
import cats.effect.Sync
import org.http4s.{ HttpRoutes, MediaType }
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

final case class ActionRoutes[F[_]: Monad: Defer: Sync]()(implicit me: MonadError[F, Throwable]) extends Http4sDsl[F] {
  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "actions" => Ok("", `Content-Type`(MediaType.application.json))
  }
}
