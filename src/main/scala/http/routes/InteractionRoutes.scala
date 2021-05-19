package http.routes

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import domain.task.TaskId
import http.middlewares.InteractionMiddleware
import http.templates.PostAnswerModalView
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.reqresp.chat.SlackApiChatPostMessageRequest
import org.latestbit.slack.morphism.client.{ SlackApiClientT, SlackApiToken }
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsOpenRequest
import org.latestbit.slack.morphism.common.SlackChannelId
import org.latestbit.slack.morphism.events.{ SlackInteractionBlockActionEvent, SlackInteractionViewSubmissionEvent }
import org.typelevel.log4cats.Logger
import service.{ ResponseService, TaskService, TokenService }

final case class InteractionRoutes[F[_]: Sync: Logger](
    tokenService: TokenService[F],
    taskService: TaskService[F],
    responseService: ResponseService[F],
    slackApiClient: SlackApiClientT[F]
) extends Http4sDsl[F] {
  val routes = InteractionMiddleware {

    case blockActionEvent: SlackInteractionBlockActionEvent =>
      tokenService.findByTeamId(blockActionEvent.team.id) >>= {
          case Some(token) =>
            val maybeActionId     = blockActionEvent.actions.flatMap(_.headOption).map(_.action_id)
            implicit val evidence = SlackApiToken.createFrom(token.`type`, token.value, Some(token.scope), Some(token.teamId))
            slackApiClient.views
              .open(
                SlackApiViewsOpenRequest(
                  trigger_id = blockActionEvent.trigger_id,
                  view = new PostAnswerModalView(maybeActionId.fold("-1")(_.value)).toModalView()
                )
              ) >>= (_.fold(_ => InternalServerError(), _ => Ok()))
          case _ => InternalServerError()
        }

    case actionSubmissionEvent: SlackInteractionViewSubmissionEvent =>
      (for {
        token <- OptionT(tokenService.findByTeamId(actionSubmissionEvent.team.id))
        implicit0(slackApiToken: SlackApiToken) = SlackApiToken.createFrom(
          token.`type`,
          token.value,
          Some(token.scope),
          Some(token.teamId)
        )
        (taskId, codeValue) <- OptionT.fromOption[F](
                                actionSubmissionEvent.view.stateParams.state
                                  .flatMap(_.values.lastOption)
                                  .flatMap { case (taskId, codeValue) => taskId.toLongOption.map((_, codeValue)) }
                              )
        task <- OptionT(taskService.findById(TaskId(taskId)))
        _ <- OptionT(
              slackApiClient.chat
                .postMessage(
                  SlackApiChatPostMessageRequest(
                    channel = SlackChannelId(s"${task.creatorId.value}"),
                    text = s"$codeValue"
                  )
                )
                .map(_.toOption)
            )
      } yield ()).foldF(InternalServerError())(_ => Ok())

    case _ => InternalServerError()
  }
}
