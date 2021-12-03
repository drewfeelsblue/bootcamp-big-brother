package config

import cats.effect.Sync
import cats.implicits.catsSyntaxTuple4Semigroupal
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.types.string.NonEmptyString
import org.typelevel.log4cats.Logger
import pureconfig.generic.auto.exportReader

final case class AppConfig(
  dbConfig: DbConfig,
  dbMigrationConfig: DbMigrationConfig,
  httpServerConfig: HttpServerConfig,
  slackAppConfig: SlackAppConfig
)

object AppConfig {
  def load[F[_]: Sync](implicit logger: Logger[F]): F[AppConfig] = {
    (
      Loader[DbConfig]("db"),
      Loader[DbMigrationConfig]("db.migration"),
      Loader[HttpServerConfig]("http.server"),
      Loader[SlackAppConfig]("slack")
    ).mapN(AppConfig.apply)
  }
}

final case class DbConfig(
  host: NonEmptyString,
  port: Port,
  user: NonEmptyString,
  password: Secret[NonEmptyString],
  databaseName: NonEmptyString,
  maxSessions: MaxSessionsNumber
) extends DetailedToString

final case class DbMigrationConfig(
  url: NonEmptyString,
  user: NonEmptyString,
  password: Secret[NonEmptyString]
) extends DetailedToString

final case class HttpServerConfig(
  host: Host,
  port: Port
) extends DetailedToString

final case class SlackAppConfig(
  clientId: NonEmptyString,
  clientSecret: Secret[NonEmptyString],
  signingSecret: Secret[NonEmptyString],
  scope: NonEmptyString,
  redirectUrl: NonEmptyString
) extends DetailedToString
