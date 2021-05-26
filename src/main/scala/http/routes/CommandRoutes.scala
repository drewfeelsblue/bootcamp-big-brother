package http.routes

import cats.effect.Concurrent
import cats.implicits._
import cats.effect.syntax.concurrent._
import domain.command.{CommandOptions, ResponseUrl}
import domain.command.CommandOptions._
import domain.task.Task
import http.templates.CommandMessage
import org.http4s.ContextRoutes
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.common.SlackResponseTypes
import service.{ResponseService, TaskService}
import org.http4s.circe.CirceEntityEncoder._
import org.latestbit.slack.morphism.messages.SlackBlock

final class CommandRoutes[F[_]: Concurrent](
  taskService: TaskService[F],
  responses: ResponseService[F],
  slackApiClient: SlackApiClientT[F]
) extends Http4sDsl[F]
    with CirceCodecs {

  val routes: ContextRoutes[CommandOptions, F] = ContextRoutes.of[CommandOptions, F] { case POST -> Root as command =>
    command match {
      case Init(topic, title, channel, creator, responseUrl) =>
        (taskService.save(Task(topic, title, channel, creator)) >>= { taskId =>
         sendReply(responseUrl, CommandMessage.successInitTask(topic, title, creator, taskId), SlackResponseTypes.InChannel)
        }).start *> Ok()

      case Report(channel, responseUrl) =>
        ((taskService.countByChannel(channel), responses.getAllUsersWithReplyCount).tupled >>= {
          case (totalTaskCount, usersWithReplyCount) =>
            sendReply(responseUrl, CommandMessage.report(totalTaskCount, usersWithReplyCount))
        }).start *> Ok()

      case SyntaxError(responseUrl) =>
        sendReply(responseUrl, CommandMessage.failInitTask).start *> Ok()
    }
  }

  private def sendReply(responseUrl: ResponseUrl, responseBlocks: Option[List[SlackBlock]], responseType: String = SlackResponseTypes.Ephemeral) =
    slackApiClient.events
      .reply(
        response_url = responseUrl.value,
        SlackApiEventMessageReply(
          text = "",
          blocks = responseBlocks,
          response_type = Some(responseType)
        )
      )
}
