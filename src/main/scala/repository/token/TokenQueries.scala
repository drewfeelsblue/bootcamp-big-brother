package repository.token

import domain.token.Token
import org.latestbit.slack.morphism.common.SlackTeamId
import repository.token.TokenCodecs.{ teamIdCodec, tokenCodec }
import skunk.{ Command, Query }
import skunk.implicits.toStringOps

object TokenQueries {
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

}
