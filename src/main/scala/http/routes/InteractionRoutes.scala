package http.routes

import cats.effect.Sync
import http.middlewares.InteractionMiddleware
import org.http4s.dsl.Http4sDsl

// TODO draft version of routes
final case class InteractionRoutes[F[_]: Sync]() extends Http4sDsl[F] {
  val routes = InteractionMiddleware {
    case _ => Ok("")
  }
}
