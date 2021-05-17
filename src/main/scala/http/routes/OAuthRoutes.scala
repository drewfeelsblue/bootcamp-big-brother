package http.routes

import cats.data.EitherT
import cats.effect.Sync
import config.SlackAppConfig
import model.Domain.Token
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.implicits.http4sLiteralsSyntax
import org.latestbit.slack.morphism.client.SlackApiClientT
import service.TokenService

final case class OAuthRoutes[F[_]: Sync](
    slackApiClient: SlackApiClientT[F],
    tokenService: TokenService[F],
    slackAppConfig: SlackAppConfig
) extends Http4sDsl[F] {
  object OAuthCodeOptParam extends OptionalQueryParamDecoderMatcher[String]("code")
  object ErrorCodeOptParam extends OptionalQueryParamDecoderMatcher[String]("error")

  private val SlackAuthUrlV2 = uri"https://slack.com/oauth/v2/authorize"

  val routes = HttpRoutes.of[F] {

    case GET -> Root / "auth" / "install" =>
      val params = Map(
        "client_id" -> slackAppConfig.clientId.value,
        "scope" -> slackAppConfig.scope.value,
        "redirect_id" -> slackAppConfig.redirectUrl.fold("")(_.value)
      )
      TemporaryRedirect(Location(SlackAuthUrlV2.withQueryParams(params)))

    case GET -> Root / "auth" / "callback" :? OAuthCodeOptParam(code) +& ErrorCodeOptParam(error) =>
      (code, error) match {
        case (Some(oauthCode), _) =>
          EitherT(
            slackApiClient.oauth.v2.access(
              slackAppConfig.clientId.value,
              slackAppConfig.clientSecret.value.value,
              oauthCode,
              slackAppConfig.redirectUrl.map(_.value)
            )
          ).semiflatTap { accessTokenResponse =>
              import accessTokenResponse._
              tokenService.save(Token(team.id, token_type, access_token, authed_user.id, scope))
            }
            .foldF(_ => InternalServerError(), _ => Ok("Installed"))
        case (_, Some(_)) => Ok()
        case _                => InternalServerError()
      }
  }
}
