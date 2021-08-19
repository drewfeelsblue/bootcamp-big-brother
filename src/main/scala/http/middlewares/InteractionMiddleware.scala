package http.middlewares

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import io.circe.parser._
import org.http4s.{ContextRoutes, HttpRoutes, Request, UrlForm}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.ContextMiddleware
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.events.SlackInteractionEvent

object InteractionMiddleware extends CirceCodecs {
  def apply[F[_]: Sync](routes: ContextRoutes[SlackInteractionEvent, F]): HttpRoutes[F] = new Http4sDsl[F] {

    def fetchEvent: Kleisli[OptionT[F, *], Request[F], SlackInteractionEvent] = Kleisli { case req @ POST -> Root =>
      OptionT
        .liftF(req.as[UrlForm])
        .subflatMap(_.getFirst("payload"))
        .subflatMap(payload => decode[SlackInteractionEvent](payload).toOption)
    }
    lazy val res = ContextMiddleware(fetchEvent).apply(routes)
  }.res
}
