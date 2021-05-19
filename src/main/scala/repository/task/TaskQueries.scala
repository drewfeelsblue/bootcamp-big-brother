package repository.task

import domain.task.{ Task, TaskId, Title, Topic }
import org.latestbit.slack.morphism.common.SlackChannelId
import repository.task.TaskCodecs.{ channelCodec, taskCodec, taskIdCodec, titleCodec, topicCodec }
import skunk.{ ~, Query }
import skunk.implicits.toStringOps

object TaskQueries {
  private val tableName = "big_brother.t_tasks"

  val save: Query[Task, TaskId] =
    sql"""
         INSERT INTO #$tableName (topic, title, channel_id, creator_id)
         VALUES ${taskCodec.values}
         RETURNING id
       """.query(taskIdCodec)

  val findByTopicAndTitleAndChannel: Query[Topic ~ Title ~ SlackChannelId, TaskId ~ Task] =
    sql"""
         SELECT id, topic, title, channel_id, creator_id
         FROM #$tableName 
         WHERE topic = $topicCodec AND title = $titleCodec AND channel_id = $channelCodec
       """.query(taskIdCodec ~ taskCodec)
}
