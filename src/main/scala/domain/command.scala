package domain

import domain.command.CommandOptions.ReportType
import domain.task.{Title, Topic}
import io.estatico.newtype.macros.newtype
import org.latestbit.slack.morphism.common.{SlackChannelId, SlackUserId}
import scala.language.implicitConversions

object command {

  @newtype case class ResponseUrl(value: String)

  sealed trait CommandOptions
  object CommandOptions {
    type ReportType = "Report"

    final case class Init(
      topic: Topic,
      title: Title,
      channel: SlackChannelId,
      creator: SlackUserId,
      responseUrl: ResponseUrl
    )                                                                          extends CommandOptions
    final case class Report(channel: SlackChannelId, responseUrl: ResponseUrl) extends CommandOptions
    final case class SyntaxError(responseUrl: ResponseUrl)                     extends CommandOptions
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
}
