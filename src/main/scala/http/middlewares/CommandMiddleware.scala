package http.middlewares

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.implicits.catsSyntaxTuple4Semigroupal
import cats.syntax.functor._
import domain.task.{Title, Topic}
import http.middlewares.CommandOptions.ReportType
import org.http4s.{Request, UrlForm}
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.common.{SlackChannelId, SlackUserId}
import org.http4s.server.ContextMiddleware

sealed trait CommandOptions
object CommandOptions {
  type ReportType = "Report"

  final case class Init(topic: Topic, title: Title, channel: SlackChannelId, creator: SlackUserId, responseUrl: String)
      extends CommandOptions
  final case class Report(channel: SlackChannelId)  extends CommandOptions
  final case class SyntaxError(responseUrl: String) extends CommandOptions
}

object CommandInitTaskSyntax {
  def unapply(text: String): Option[(Topic, Title)] = text.trim.split(" ").filterNot(_.isEmpty).toList match {
    case topic :: title :: Nil => Some(Topic(topic), Title(title))
    case _                     => None
  }
}

object CommandReportSyntax {
  def unapply(text: String): Option[ReportType] = text.trim.split(" ").filterNot(_.isEmpty).toList match {
    case "report" :: Nil => Some("Report")
    case _               => None
  }
}

trait CommandMiddleware[F[_]] extends Http4sDsl[F] {
  import CommandOptions._
  private def fetchCommand(implicit S: Sync[F]): Kleisli[OptionT[F, *], Request[F], CommandOptions] = Kleisli {
    case req @ POST -> Root =>
      OptionT.liftF(
        req
          .as[UrlForm]
          .map { form =>
            (
              form.getFirst("text"),
              form.getFirst("channel_id"),
              form.getFirst("user_id"),
              form.getFirst("response_url")
            ).tupled match {
              case Some((CommandInitTaskSyntax(topic, title), channelId, userId, responseUrl)) =>
                Init(topic, title, SlackChannelId(channelId), SlackUserId(userId), responseUrl)
              case Some((CommandReportSyntax(_), channelId, _, _)) =>
                Report(SlackChannelId(channelId))
              case Some((_, _, _, responseUrl)) => SyntaxError(responseUrl)
            }
          }
      )
  }

  def commandMiddleware(implicit S: Sync[F]) = ContextMiddleware(fetchCommand)
}
