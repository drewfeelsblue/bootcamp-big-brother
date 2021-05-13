package config

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{ Interval, Positive }
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.string.IPv4
import pureconfig.generic.auto.exportReader
import pureconfig.ConfigSource

case class DbConfig(
    dbHost: DbConfig.Host,
    dbPort: DbConfig.Port,
    dbUser: NonEmptyString,
    dbPassword: NonEmptyString,
    database: NonEmptyString,
    maxSessions: DbConfig.MaxSessionsNumber
)
object DbConfig {
  type Port              = Int Refined Interval.Closed[1000, 9999]
  type MaxSessionsNumber = Int Refined Positive
  type Host              = String Refined IPv4

  implicit val reader = exportReader[DbConfig]
  def load: DbConfig  = ConfigSource.default.loadOrThrow[DbConfig]
}
