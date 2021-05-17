package repository.token

import model.Domain.Token
import org.latestbit.slack.morphism.common.{
  SlackAccessTokenValue,
  SlackApiTokenScope,
  SlackApiTokenType,
  SlackTeamId,
  SlackUserId
}
import skunk.Codec
import skunk.codec.all.text

object TokenCodecs {
  val teamIdCodec: Codec[SlackTeamId] = text.imap(SlackTeamId.apply)(_.value)
  val typeCodec: Codec[SlackApiTokenType] = text.imap[SlackApiTokenType] {
    case "user" => SlackApiTokenType.User
    case "bot"  => SlackApiTokenType.Bot
    case "app"  => SlackApiTokenType.App
  }(_.name)
  val valueCodec: Codec[SlackAccessTokenValue] = text.imap(SlackAccessTokenValue.apply)(_.value)
  val userIdCodec: Codec[SlackUserId]          = text.imap(SlackUserId.apply)(_.value)
  val scopeCodec: Codec[SlackApiTokenScope]    = text.imap(SlackApiTokenScope.apply)(_.value)

  val tokenCodec: Codec[Token] = (teamIdCodec ~ typeCodec ~ valueCodec ~ userIdCodec ~ scopeCodec).gimap[Token]
}
