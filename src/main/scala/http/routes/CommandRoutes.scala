package http.routes

import cats.effect.Concurrent
import cats.implicits._
import cats.effect.syntax.concurrent._
import domain.task.Task
import http.middlewares.{CommandMiddleware, CommandOptions}
import http.templates.InitTaskMessage
import org.http4s.ContextRoutes
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.common.SlackResponseTypes
import service.{ResponseService, TaskService}
import org.http4s.circe.CirceEntityEncoder._

final class CommandRoutes[F[_]: Concurrent](
  taskService: TaskService[F],
  responses: ResponseService[F],
  slackApiClient: SlackApiClientT[F]
) extends Http4sDsl[F]
    with CirceCodecs {
  import CommandOptions._

  val routes: ContextRoutes[CommandOptions, F] = ContextRoutes.of[CommandOptions, F] { case req @ POST -> Root / "command" as command =>
    command match {
      case Init(topic, title, channel, creator, responseUrl) =>
        (taskService.save(Task(topic, title, channel, creator)) >>= { taskId =>
          slackApiClient.events.reply(
            response_url = responseUrl,
            SlackApiEventMessageReply(
              text = "",
              blocks = InitTaskMessage.success(topic, title, creator, taskId),
              response_type = Some(SlackResponseTypes.InChannel)
            )
          )
        }).start *> Ok()

      case Report(channel) =>
        taskService.countByChannel(channel) >>= { repliesCount =>
          responses.getAllUsersWithReplyCount.map(_.map((_, repliesCount)))
        } >>= { response => Ok(s"$response") }

      case SyntaxError => Ok(InitTaskMessage.fail)
    }
  }
}
