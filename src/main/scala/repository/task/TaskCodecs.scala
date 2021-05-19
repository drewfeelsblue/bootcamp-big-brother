package repository.task

import domain.task.{ Task, TaskId, Title, Topic }
import org.latestbit.slack.morphism.common.{ SlackChannelId, SlackUserId }
import skunk.Codec
import skunk.codec.all.{ int8, text }
import repository.ext._

object TaskCodecs {
  val taskIdCodec: Codec[TaskId]          = int8
  val topicCodec: Codec[Topic]            = text
  val titleCodec: Codec[Title]            = text
  val channelCodec: Codec[SlackChannelId] = text.imap(SlackChannelId.apply)(_.value)
  val creatorCodec: Codec[SlackUserId]    = text.imap(SlackUserId.apply)(_.value)

  val taskCodec: Codec[Task] = (topicCodec ~ titleCodec ~ channelCodec ~ creatorCodec).gimap[Task]
}
