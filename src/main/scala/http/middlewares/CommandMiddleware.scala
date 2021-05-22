package http.middlewares

import cats.effect.Sync
import cats.implicits.catsSyntaxTuple4Semigroupal
import cats.syntax.flatMap._
import domain.task.{Title, Topic}
import http.middlewares.CommandOptions.ReportType
import org.http4s.{HttpRoutes, Response, UrlForm}
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.common.{SlackChannelId, SlackUserId}

sealed trait CommandOptions
object CommandOptions {
  type ReportType = "Report"

  final case class Init(topic: Topic, title: Title, channel: SlackChannelId, creator: SlackUserId, responseUrl: String)
      extends CommandOptions
  final case class Report(channel: SlackChannelId) extends CommandOptions
  case object SyntaxError                          extends CommandOptions
}

object CommandInitTaskSyntax {
  def unapply(text: String): Option[(Topic, Title)] = text.trim.split(" ").map(_.trim).toList match {
    case topic :: title :: Nil => Some(Topic(topic), Title(title))
    case _                     => None
  }
}

object CommandReportSyntax {
  def unapply(text: String): Option[ReportType] = text.trim.split(" ").map(_.trim).toList match {
    case "report" :: Nil => Some("Report")
    case _               => None
  }
}

object CommandMiddleware {
  import CommandOptions._

  def apply[F[_]: Sync](resp: CommandOptions => F[Response[F]]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "command" =>
      req
        .as[UrlForm]
        .flatMap { form =>
          (
            form.getFirst("text"),
            form.getFirst("channel_id"),
            form.getFirst("user_id"),
            form.getFirst("response_url")
          ).tupled match {
            case Some((CommandInitTaskSyntax(topic, title), channelId, userId, responseUrl)) =>
              resp(Init(topic, title, SlackChannelId(channelId), SlackUserId(userId), responseUrl))
            case Some((CommandReportSyntax(_), channelId, _, _)) =>
              resp(Report(SlackChannelId(channelId)))
            case _ => resp(SyntaxError)
          }
        }
    }
  }
}
