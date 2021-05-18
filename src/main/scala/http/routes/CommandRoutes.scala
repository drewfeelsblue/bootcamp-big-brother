package http.routes

import cats.effect.Sync
import cats.implicits._
import domain.task.Task
import http.middlewares.{ CommandMiddleware, CommandOptions }
import http.templates.InitTaskResponse
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.common.SlackResponseTypes
import service.TaskService

final case class CommandRoutes[F[_]: Sync](taskService: TaskService[F], slackApiClient: SlackApiClientT[F]) extends Http4sDsl[F] {
  import CommandOptions._

  val routes: HttpRoutes[F] =
    CommandMiddleware {
      case Init(topic, title, creator, responseUrl) =>
        val response = new InitTaskResponse(topic, title, creator)
        slackApiClient.events.reply(
          response_url = responseUrl,
          SlackApiEventMessageReply(
            text = response.renderPlainText(),
            blocks = response.renderBlocks(),
            response_type = Some(SlackResponseTypes.InChannel)
          )
        ) *> taskService.save(Task(topic, title, creator)) *> Ok()
      case SyntaxError => Ok("Invalid syntax")
    }
}
