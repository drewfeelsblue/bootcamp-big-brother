package service.queries

import domain.task.{Task, TaskId, Title, Topic}
import org.latestbit.slack.morphism.common.{SlackChannelId, SlackUserId}
import skunk.codec.all.{int8, text}
import skunk.implicits.toStringOps
import skunk.{~, Codec, Query}

object TaskQueries {
  import codecs._
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

  val findById: Query[TaskId, Task] =
    sql"""
         SELECT topic, title, channel_id, creator_id
         FROM #$tableName
         WHERE id = $taskIdCodec
       """.query(taskCodec)

  object codecs {
    val taskIdCodec: Codec[TaskId]          = int8
    val topicCodec: Codec[Topic]            = text
    val titleCodec: Codec[Title]            = text
    val channelCodec: Codec[SlackChannelId] = text
    val creatorCodec: Codec[SlackUserId]    = text

    val taskCodec: Codec[Task] = (topicCodec ~ titleCodec ~ channelCodec ~ creatorCodec).gimap[Task]
  }
}
