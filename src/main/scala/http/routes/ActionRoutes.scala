package http.routes

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final case class ActionRoutes[F[_]: Sync]() extends Http4sDsl[F] {
  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "interactions" => Ok("")
  }
}
