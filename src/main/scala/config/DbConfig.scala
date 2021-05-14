package config

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import config.DbConfig.path
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{ Interval, Positive }
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import org.typelevel.log4cats.Logger
import pureconfig.generic.auto.exportReader
import pureconfig.ConfigSource

case class DbConfig(
    host: NonEmptyString,
    port: DbConfig.Port,
    user: NonEmptyString,
    password: Secret[NonEmptyString],
    databaseName: NonEmptyString,
    maxSessions: DbConfig.MaxSessionsNumber
) extends DetailedToString

object DbConfig {
  private val path = "db"
  type Port              = Int Refined Interval.Closed[1000, 9999]
  type MaxSessionsNumber = Int Refined Positive

  def load[F[_]: Sync: Logger]: F[DbConfig] =
    for {
      config <- Sync[F].delay(ConfigFactory.load().getConfig(path))
      dbConfig <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[DbConfig])
      _ <- Logger[F].info(s"Database config loaded($dbConfig)")
    } yield dbConfig
}
