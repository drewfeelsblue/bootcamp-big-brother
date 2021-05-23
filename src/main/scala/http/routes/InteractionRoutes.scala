package http.routes

import cats.Order
import cats.data.{NonEmptySet, OptionT}
import cats.effect.Concurrent
import cats.implicits._
import cats.effect.syntax.concurrent._
import domain.response.Response
import domain.task.TaskId
import domain.token.Token
import http.middlewares.InteractionMiddleware
import http.templates.PostAnswerModalView
import io.circe.Json
import org.http4s.ContextRoutes
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.reqresp.files.{SlackApiFilesUploadRequest, SlackApiFilesUploadResponse}
import org.latestbit.slack.morphism.client.reqresp.users.{SlackApiUsersInfoRequest, SlackApiUsersInfoResponse}
import org.latestbit.slack.morphism.client.{SlackApiClientT, SlackApiToken}
import org.latestbit.slack.morphism.client.reqresp.views.{SlackApiViewsOpenRequest, SlackApiViewsOpenResponse}
import org.latestbit.slack.morphism.common.{
  SlackActionId,
  SlackChannelId,
  SlackFileType,
  SlackTriggerId,
  SlackUserId,
  SlackUserInfo
}
import org.latestbit.slack.morphism.events.{
  SlackInteractionBlockActionEvent,
  SlackInteractionEvent,
  SlackInteractionViewSubmissionEvent
}
import org.typelevel.log4cats.Logger
import service.{ResponseService, TaskService, TokenService}

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets

final class InteractionRoutes[F[_]: Concurrent: Logger](
  tokenService: TokenService[F],
  taskService: TaskService[F],
  responseService: ResponseService[F],
  slackApiClient: SlackApiClientT[F]
) extends Http4sDsl[F]
    with InteractionMiddleware[F] {
  implicit val slackChannelIdOrder: Order[SlackChannelId] = Order.by[SlackChannelId, String](_.value)

  val routes: ContextRoutes[SlackInteractionEvent, F] = ContextRoutes.of[SlackInteractionEvent, F] {
    case POST -> Root / "interaction" as event =>
      event match {

        case blockActionEvent: SlackInteractionBlockActionEvent =>
          (for {
            token                                  <- OptionT(tokenService.findByTeamId(blockActionEvent.team.id))
            actionId                               <- OptionT.fromOption[F](blockActionEvent.actions.flatMap(_.headOption).map(_.action_id))
            implicit0(slackApiToken: SlackApiToken) = createSlackApiToken(token)
            _                                      <- openCodeSubmissionView(blockActionEvent.trigger_id, actionId)
          } yield ()).value.start *> Ok()

        case actionSubmissionEvent: SlackInteractionViewSubmissionEvent =>
          val senderId = actionSubmissionEvent.user.id
          (for {
            token                                  <- OptionT(tokenService.findByTeamId(actionSubmissionEvent.team.id))
            implicit0(slackApiToken: SlackApiToken) = createSlackApiToken(token)
            (taskId, codeInputStream)              <- fetchFromEvent(actionSubmissionEvent)
            task                                   <- OptionT(taskService.findById(taskId))
            _                                      <- OptionT.liftF(responseService.save(Response(taskId, senderId)))
            senderInfo                             <- getSenderInfo(senderId)
            _                                      <- sendCode(task.creatorId, senderInfo.user, codeInputStream)
          } yield ()).value.start *> Ok()

        case _ => InternalServerError()
      }
  }

  private def openCodeSubmissionView(eventTriggerId: SlackTriggerId, taskId: SlackActionId)(implicit
    token: SlackApiToken
  ): OptionT[F, SlackApiViewsOpenResponse] = OptionT(
    slackApiClient.views
      .open(SlackApiViewsOpenRequest(eventTriggerId, new PostAnswerModalView(taskId.value).toModalView()))
      .map(_.toOption)
  )

  private def fetchFromEvent(event: SlackInteractionViewSubmissionEvent): OptionT[F, (TaskId, InputStream)] =
    OptionT.fromOption[F] {
      event.view.stateParams.state
        .flatMap(_.values.lastOption)
        .flatMap { case (taskId, codeValue) => taskId.toLongOption.map(id => (TaskId(id), codeValue)) }
    } >>= { case (taskId, codeValue) => decodeSubmittedCode(codeValue).map((taskId, _)) }

  private def decodeSubmittedCode(code: Json): OptionT[F, InputStream] = OptionT.fromOption[F](
    code.hcursor
      .downField("code")
      .downField("value")
      .as[String]
      .map(_.getBytes(StandardCharsets.UTF_8))
      .map(new ByteArrayInputStream(_))
      .toOption
  )

  private def createSlackApiToken(token: Token): SlackApiToken = {
    import token._
    SlackApiToken.createFrom(`type`, value, Some(scope), Some(teamId))
  }

  private def getSenderInfo(id: SlackUserId)(implicit token: SlackApiToken): OptionT[F, SlackApiUsersInfoResponse] =
    OptionT {
      slackApiClient.users.info(SlackApiUsersInfoRequest(id)).map(_.toOption)
    }

  private def sendCode(creatorId: SlackUserId, senderInfo: SlackUserInfo, codeInputStream: InputStream)(implicit
    token: SlackApiToken
  ): OptionT[F, SlackApiFilesUploadResponse] = OptionT {
    slackApiClient.files
      .upload(
        SlackApiFilesUploadRequest(
          channels = Some(NonEmptySet.one(SlackChannelId(s"${creatorId.value}"))),
          filename = s"${senderInfo.name.getOrElse("Anonymous")}",
          filetype = Some(SlackFileType.Scala)
        ),
        codeInputStream
      )
      .map(_.toOption)
  }
}
