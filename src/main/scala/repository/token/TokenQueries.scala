package repository.token

import model.Domain.Token
import repository.token.TokenCodecs.tokenCodec
import skunk.Command
import skunk.implicits.toStringOps

object TokenQueries {
  private val tableName = "big_brother.t_tokens"

  val save: Command[Token] =
    sql"""
         INSERT INTO #$tableName (team_id, type, value, user_id, scope)
         VALUES ${tokenCodec.values}
         """.command

}
