package http.middlewares

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.implicits.catsSyntaxTuple4Semigroupal
import cats.syntax.functor._
import domain.command.CommandOptions._
import domain.command.{CommandInitTaskSyntax, CommandOptions, CommandReportSyntax, ResponseUrl}
import org.http4s.{Request, UrlForm}
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.common.{SlackChannelId, SlackUserId}
import org.http4s.server.ContextMiddleware

trait CommandMiddleware[F[_]] extends Http4sDsl[F] {
  private def fetchCommand(implicit S: Sync[F]): Kleisli[OptionT[F, *], Request[F], CommandOptions] = Kleisli {
    case req @ POST -> Root =>
      OptionT.liftF(
        req
          .as[UrlForm]
          .map { form =>
            (
              form.getFirst("text"),
              form.getFirst("channel_id").map(SlackChannelId.apply),
              form.getFirst("user_id").map(SlackUserId.apply),
              form.getFirst("response_url").map(ResponseUrl.apply)
            ).tupled match {
              case Some((CommandInitTaskSyntax(topic, title), channelId, userId, responseUrl)) =>
                Init(topic, title, channelId, userId, responseUrl)
              case Some((CommandReportSyntax(_), channelId, _, responseUrl)) =>
                Report(channelId, responseUrl)
              case Some((_, _, _, responseUrl)) => SyntaxError(responseUrl)
            }
          }
      )
  }

  def commandMiddleware(implicit S: Sync[F]) = ContextMiddleware(fetchCommand)
}
