package http.routes

import cats.effect.Sync
import cats.implicits._
import domain.task.Task
import http.middlewares.{ CommandMiddleware, CommandOptions }
import http.templates.InitTaskMessage
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.common.SlackResponseTypes
import service.TaskService

final case class CommandRoutes[F[_]: Sync](taskService: TaskService[F], slackApiClient: SlackApiClientT[F])
    extends Http4sDsl[F]
    with CirceCodecs {
  import CommandOptions._
  implicit val slackApiEventMessageReplyEncoder: EntityEncoder[F, SlackApiEventMessageReply] = jsonEncoderOf

  val routes = CommandMiddleware {
    case Init(topic, title, channel, creator, responseUrl) =>
      slackApiClient.events.reply(
        response_url = responseUrl,
        SlackApiEventMessageReply(
          text = "",
          blocks = InitTaskMessage.success(topic, title, creator),
          response_type = Some(SlackResponseTypes.InChannel)
        )
      ) *> taskService.save(Task(topic, title, channel, creator)) *> Ok()
    case SyntaxError => Ok(InitTaskMessage.fail)
  }
}
