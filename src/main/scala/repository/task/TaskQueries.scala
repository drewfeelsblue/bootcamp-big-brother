package repository.task

import domain.task.Task
import repository.task.TaskCodecs.taskCodec
import skunk.Command
import skunk.implicits.toStringOps

object TaskQueries {
  private val tableName = "big_brother.t_tasks"

  val save: Command[Task] =
    sql"""
         INSERT INTO #$tableName (topic, title, creator_id)
         VALUES ${taskCodec.values}
       """.command
}
