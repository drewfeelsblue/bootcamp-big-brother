package http.middlewares

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import io.circe.parser._
import org.http4s.{Request, UrlForm}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.ContextMiddleware
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.events.SlackInteractionEvent

trait InteractionMiddleware[F[_]] extends CirceCodecs with Http4sDsl[F] {
  private def fetchEvent(implicit S: Sync[F]): Kleisli[OptionT[F, *], Request[F], SlackInteractionEvent] =
    Kleisli { case req @ POST -> Root / "interaction" =>
      OptionT
        .liftF(req.as[UrlForm])
        .subflatMap(_.getFirst("payload"))
        .subflatMap(payload => decode[SlackInteractionEvent](payload).toOption)
    }

  def interactionMiddleware(implicit S: Sync[F]) = ContextMiddleware(fetchEvent)
}
