package http.routes

import cats.Order
import cats.data.{EitherT, NonEmptySet, OptionT}
import cats.effect.{Concurrent, ConcurrentEffect}
import cats.implicits._
import cats.effect.implicits._
import domain.response.Response
import domain.task.TaskId
import domain.token.Token
import http.middlewares.InteractionMiddleware
import http.templates.PostAnswerModalView
import io.circe.{Decoder, Json}
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.reqresp.files.{SlackApiFilesUploadRequest, SlackApiFilesUploadResponse}
import org.latestbit.slack.morphism.client.reqresp.users.{SlackApiUsersInfoRequest, SlackApiUsersInfoResponse}
import org.latestbit.slack.morphism.client.{SlackApiClientT, SlackApiToken}
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsOpenRequest
import org.latestbit.slack.morphism.common.{SlackChannelId, SlackFileType, SlackUserId, SlackUserInfo}
import org.latestbit.slack.morphism.events.{SlackInteractionBlockActionEvent, SlackInteractionViewSubmissionEvent}
import org.typelevel.log4cats.Logger
import service.{ResponseService, TaskService, TokenService}

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets

final class InteractionRoutes[F[_]: Concurrent: Logger](
  tokenService: TokenService[F],
  taskService: TaskService[F],
  responseService: ResponseService[F],
  slackApiClient: SlackApiClientT[F]
) extends Http4sDsl[F] {
  implicit val slackChannelIdOrder: Order[SlackChannelId] = Order.by[SlackChannelId, String](_.value)

  val routes = InteractionMiddleware {

    case blockActionEvent: SlackInteractionBlockActionEvent =>
      tokenService.findByTeamId(blockActionEvent.team.id) >>= {
        case Some(token) =>
          val maybeActionId = blockActionEvent.actions.flatMap(_.headOption).map(_.action_id)
          implicit val evidence =
            SlackApiToken.createFrom(token.`type`, token.value, Some(token.scope), Some(token.teamId))
          slackApiClient.views
            .open(
              SlackApiViewsOpenRequest(
                trigger_id = blockActionEvent.trigger_id,
                view = new PostAnswerModalView(maybeActionId.fold("-1")(_.value)).toModalView()
              )
            )
            .start *> Ok()
        case _ => InternalServerError()
      }

    case actionSubmissionEvent: SlackInteractionViewSubmissionEvent =>
      val senderId = actionSubmissionEvent.user.id
      (for {
        token                                  <- OptionT(tokenService.findByTeamId(actionSubmissionEvent.team.id))
        implicit0(slackApiToken: SlackApiToken) = createSlackApiToken(token)

        (taskId, codeValue) <- OptionT.fromOption[F](
          actionSubmissionEvent.view.stateParams.state
            .flatMap(_.values.lastOption)
            .flatMap { case (taskId, codeValue) => taskId.toLongOption.map((_, codeValue)) }
        )
        task            <- OptionT(taskService.findById(TaskId(taskId)))
        _               <- OptionT.liftF(responseService.save(Response(TaskId(taskId), senderId)))
        senderInfo      <- getSenderInfo(senderId)
        codeInputStream <- decodeSubmittedCode(codeValue)
        _               <- sendCode(task.creatorId, senderInfo.user, codeInputStream)
      } yield ()).value.start *> Ok()

    case _ => InternalServerError()
  }

  private def decodeSubmittedCode(code: Json): OptionT[F, InputStream] = OptionT.fromOption[F](
    code.hcursor
      .downField("code")
      .downField("value")
      .as[String]
      .map(_.getBytes(StandardCharsets.UTF_8))
      .map(new ByteArrayInputStream(_))
      .toOption
  )

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

  private def getSenderInfo(id: SlackUserId)(implicit token: SlackApiToken): OptionT[F, SlackApiUsersInfoResponse] =
    OptionT {
      slackApiClient.users.info(SlackApiUsersInfoRequest(id)).map(_.toOption)
    }

  private def createSlackApiToken(token: Token): SlackApiToken = {
    import token._
    SlackApiToken.createFrom(`type`, value, Some(scope), Some(teamId))
  }
}
