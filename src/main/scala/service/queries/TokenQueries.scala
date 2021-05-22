package service.queries

import domain.token.Token
import org.latestbit.slack.morphism.common._
import skunk.codec.all.text
import skunk.implicits.toStringOps
import skunk.{Codec, Command, Query}

object TokenQueries {
  import codecs._
  private val tableName = "big_brother.t_tokens"

  val save: Command[Token] =
    sql"""
         INSERT INTO #$tableName (team_id, type, value, user_id, scope)
         VALUES ${tokenCodec.values}
       """.command

  def findByTeamId: Query[SlackTeamId, Token] =
    sql"""
         SELECT team_id, type, value, user_id, scope
         FROM #$tableName
         WHERE team_id = $teamIdCodec
       """.query(tokenCodec)

  object codecs {
    val teamIdCodec: Codec[SlackTeamId] = text
    val typeCodec: Codec[SlackApiTokenType] = text.imap[SlackApiTokenType] {
      case "user" => SlackApiTokenType.User
      case "bot"  => SlackApiTokenType.Bot
      case "app"  => SlackApiTokenType.App
    }(_.name)
    val valueCodec: Codec[SlackAccessTokenValue] = text
    val userIdCodec: Codec[SlackUserId]          = text
    val scopeCodec: Codec[SlackApiTokenScope]    = text

    val tokenCodec: Codec[Token] = (teamIdCodec ~ typeCodec ~ valueCodec ~ userIdCodec ~ scopeCodec).gimap[Token]
  }
}
