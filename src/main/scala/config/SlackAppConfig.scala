package config

import eu.timepit.refined.types.string.NonEmptyString

final case class SlackAppConfig(
    clientId: NonEmptyString,
    clientSecret: Secret[NonEmptyString],
    signingSecret: Secret[NonEmptyString],
    scope: NonEmptyString,
    redirectUrl: Option[NonEmptyString] = None
) extends DetailedToString
