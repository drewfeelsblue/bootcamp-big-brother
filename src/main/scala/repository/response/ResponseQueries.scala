package repository.response

import domain.response.Response
import domain.task.TaskId
import org.latestbit.slack.morphism.common.SlackUserId
import repository.response.ResponseCodecs.{ responseCodec, taskIdCodec, userIdCodec }
import skunk.implicits.toStringOps
import skunk.{ ~, Command, Query }

object ResponseQueries {
  private val tableName = "big_brother.t_responses"

  val save: Command[Response] =
    sql"""
         INSERT INTO #$tableName (task_id, user_id)
         VALUES ${responseCodec.values}
       """.command

  val findByTaskIdAndUserId: Query[TaskId ~ SlackUserId, Response] =
    sql"""
         SELECT task_id, user_id
         FROM #$tableName
         WHERE task_id = $taskIdCodec AND user_id = $userIdCodec
       """.query(responseCodec)
}
