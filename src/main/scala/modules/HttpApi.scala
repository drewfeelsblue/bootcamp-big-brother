package modules

import cats.effect.Concurrent
import cats.implicits.toSemigroupKOps
import config.SlackAppConfig
import http.routes.{CommandRoutes, InteractionRoutes, OAuthRoutes}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.middleware.Logger
import org.latestbit.slack.morphism.client.SlackApiClientT

sealed abstract case class HttpApi[F[_]: Concurrent: org.typelevel.log4cats.Logger] private (
  services: Services[F],
  slackApiClient: SlackApiClientT[F],
  slackAppConfig: SlackAppConfig
) {
  import services._

  private val oauthRoutes       = new OAuthRoutes(slackApiClient, tokens, slackAppConfig)
  private val commandRoutes     = new CommandRoutes(tasks, slackApiClient)
  private val interactionRoutes = new InteractionRoutes(tokens, tasks, responses, slackApiClient)

  val routes =
    Logger.httpApp(true, true)((oauthRoutes.routes <+> commandRoutes.routes <+> interactionRoutes.routes).orNotFound)
}

object HttpApi {
  def apply[F[_]: Concurrent: org.typelevel.log4cats.Logger](
    services: Services[F],
    slackApiClientT: SlackApiClientT[F],
    slackAppConfig: SlackAppConfig
  ) = new HttpApi[F](services, slackApiClientT, slackAppConfig) {}.routes
}
