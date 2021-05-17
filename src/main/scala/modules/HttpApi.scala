package modules

import cats.effect.{ Concurrent, Sync }
import config.SlackAppConfig
import http.routes.OAuthRoutes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.middleware.Logger
import org.latestbit.slack.morphism.client.SlackApiClientT

final class HttpApi[F[_]: Sync: Concurrent] private (
    services: Services[F],
    slackApiClientT: SlackApiClientT[F],
    slackAppConfig: SlackAppConfig
) {

  private val oauthRoutes = OAuthRoutes(slackApiClientT, services.tokens, slackAppConfig)

  val routes = Logger.httpApp(true, true)(oauthRoutes.routes.orNotFound)
}

object HttpApi {
  def make[F[_]: Sync: Concurrent](services: Services[F], slackApiClientT: SlackApiClientT[F], slackAppConfig: SlackAppConfig) =
    new HttpApi[F](services, slackApiClientT, slackAppConfig)
}
