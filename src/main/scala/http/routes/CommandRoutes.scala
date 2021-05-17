package http.routes

import cats.effect.Sync
import cats.{ Applicative, Defer, Monad, MonadError }
import cats.syntax.functor._
import cats.syntax.flatMap._
import http.Templates
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

final case class CommandRoutes[F[_]: Monad: Defer: Sync]()(implicit me: MonadError[F, Throwable]) extends Http4sDsl[F] {

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "command" => Ok("", `Content-Type`(MediaType.application.json))
  }
}
