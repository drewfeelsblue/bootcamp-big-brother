package http.routes

import cats.effect.Sync
import cats.implicits._
import domain.response.Response
import domain.task.TaskId
import http.middlewares.InteractionMiddleware
import http.templates.PostAnswerModalView
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.{ SlackApiClientT, SlackApiToken }
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsOpenRequest
import org.latestbit.slack.morphism.common.{ SlackActionId, SlackBlockId }
import org.latestbit.slack.morphism.events.{ SlackInteractionBlockActionEvent, SlackInteractionViewSubmissionEvent }
import org.typelevel.log4cats.Logger
import service.{ ResponseService, TokenService }

final case class InteractionRoutes[F[_]: Sync: Logger](
    tokenService: TokenService[F],
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
      actionSubmissionEvent.view.stateParams.state.flatMap(_.values.lastOption) match {
        case Some((taskId, codeValue)) =>
          Logger[F].info(s"$taskId - $codeValue") *> Ok()
        case _ => InternalServerError()
      }
    case _ => InternalServerError()
  }
}
