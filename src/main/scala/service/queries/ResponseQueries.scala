package service.queries

import domain.response.Response
import domain.task.TaskId
import org.latestbit.slack.morphism.common.SlackUserId
import skunk.codec.all.{int8, text}
import skunk.implicits.toStringOps
import skunk.{~, Codec, Command, Query}

object ResponseQueries {
  import codecs._
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

  object codecs {
    val taskIdCodec: Codec[TaskId]      = int8
    val userIdCodec: Codec[SlackUserId] = text.imap(SlackUserId.apply)(_.value)
    val responseCodec: Codec[Response]  = (taskIdCodec ~ userIdCodec).gimap[Response]
  }
}
