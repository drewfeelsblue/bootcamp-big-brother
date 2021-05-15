package config

import eu.timepit.refined.types.string.NonEmptyString

case class DbMigrationConfig(
    url: NonEmptyString,
    user: NonEmptyString,
    password: Secret[NonEmptyString]
) extends DetailedToString
