package config

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import org.typelevel.log4cats.Logger
import pureconfig.generic.auto.exportReader
import pureconfig.ConfigSource

case class DbMigrationConfig(
    url: NonEmptyString,
    user: NonEmptyString,
    password: Secret[NonEmptyString]
) extends DetailedToString

object DbMigrationConfig {
  private val path = "db.migration"
  def load[F[_]: Sync: Logger]: F[DbMigrationConfig] =
    for {
      config <- Sync[F].delay(ConfigFactory.load().getConfig(path))
      dbMigrationConfig <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[DbMigrationConfig])
      _ <- Logger[F].info(s"Database migration config loaded($dbMigrationConfig)")
    } yield dbMigrationConfig
}
