package config

import cats.effect.Sync
import com.typesafe.config.ConfigFactory
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import pureconfig.generic.auto.exportReader
import pureconfig.ConfigSource

case class DbMigrationConfig(
    url: NonEmptyString,
    user: NonEmptyString,
    password: NonEmptyString
)
object DbMigrationConfig {
  private val path = "db.migration"
  def load[F[_]: Sync]: F[DbMigrationConfig] = Sync[F].delay {
    val config = ConfigFactory.load().getConfig(path)
    ConfigSource.fromConfig(config).loadOrThrow[DbMigrationConfig]
  }
}
