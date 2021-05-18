package http.middlewares

import cats.data.OptionT
import cats.effect.Sync
import io.circe.parser._
import org.http4s.{ HttpRoutes, Response, UrlForm }
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.events.SlackInteractionEvent

object InteractionMiddleware extends CirceCodecs {
  def apply[F[_]: Sync](resp: SlackInteractionEvent => F[Response[F]]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "interaction" =>
        OptionT
          .liftF(req.as[UrlForm])
          .subflatMap(_.getFirst("payload"))
          .subflatMap(payload => decode[SlackInteractionEvent](payload).toOption)
          .foldF(BadRequest())(resp)
    }
  }
}
