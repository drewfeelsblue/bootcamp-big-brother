package repository.task

import domain.task.{ Task, Title, Topic }
import repository.task.TaskCodecs.{ taskCodec, titleCodec, topicCodec }
import skunk.{ ~, Command, Query }
import skunk.implicits.toStringOps

object TaskQueries {
  private val tableName = "big_brother.t_tasks"

  val save: Command[Task] =
    sql"""
         INSERT INTO #$tableName (topic, title, creator_id)
         VALUES ${taskCodec.values}
       """.command

  val findByTopicAndTitle: Query[Topic ~ Title, Task] =
    sql"""
         SELECT topic, title, creator_id
         FROM #$tableName 
         WHERE topic = $topicCodec AND title = $titleCodec
       """.query(taskCodec)
}
