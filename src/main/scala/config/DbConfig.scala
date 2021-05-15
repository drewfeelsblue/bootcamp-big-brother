package config

import eu.timepit.refined.types.string.NonEmptyString

case class DbConfig(
    host: NonEmptyString,
    port: Port,
    user: NonEmptyString,
    password: Secret[NonEmptyString],
    databaseName: NonEmptyString,
    maxSessions: MaxSessionsNumber
) extends DetailedToString
