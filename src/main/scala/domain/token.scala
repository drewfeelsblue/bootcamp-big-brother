package domain

import org.latestbit.slack.morphism.common.{
  SlackAccessTokenValue,
  SlackApiTokenScope,
  SlackApiTokenType,
  SlackTeamId,
  SlackUserId
}

object token {
  final case class Token(
    teamId: SlackTeamId,
    `type`: SlackApiTokenType,
    value: SlackAccessTokenValue,
    userId: SlackUserId,
    scope: SlackApiTokenScope
  )
}
