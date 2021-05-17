package model

import org.latestbit.slack.morphism.common.{
  SlackAccessTokenValue,
  SlackApiTokenScope,
  SlackApiTokenType,
  SlackTeamId,
  SlackUserId
}

object Domain {
  final case class Token(
      teamId: SlackTeamId,
      `type`: SlackApiTokenType,
      value: SlackAccessTokenValue,
      user_id: SlackUserId,
      scope: SlackApiTokenScope
  )
}
