package modules

import cats.effect.Concurrent
import cats.implicits.toSemigroupKOps
import config.SlackAppConfig
import http.middlewares.{CommandMiddleware, InteractionMiddleware}
import http.routes.{CommandRoutes, InteractionRoutes, OAuthRoutes}
import org.http4s.HttpApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.latestbit.slack.morphism.client.SlackApiClientT

object HttpApi {
  def routes[F[_]: Concurrent: org.typelevel.log4cats.Logger](
    services: Services[F],
    slackApiClient: SlackApiClientT[F],
    slackAppConfig: SlackAppConfig
  ): HttpApp[F] = {
    import services._

    val oauthRoutes       = new OAuthRoutes(slackApiClient, tokens, slackAppConfig)
    val commandRoutes     = new CommandRoutes(tasks, responses, slackApiClient)
    val interactionRoutes = new InteractionRoutes(tokens, tasks, responses, slackApiClient)

    Logger.httpApp(true, true)(
      Router(
        "/command"     -> CommandMiddleware(commandRoutes.routes),
        "/interaction" -> InteractionMiddleware(interactionRoutes.routes),
        "/auth"        -> oauthRoutes.routes
      ).orNotFound
    )
  }
}
