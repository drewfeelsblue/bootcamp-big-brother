package repository.task

import domain.task.{ Task, Title, Topic }
import org.latestbit.slack.morphism.common.{ SlackChannelId, SlackUserId }
import skunk.Codec
import skunk.codec.all.text
import repository.ext._

object TaskCodecs {
  val topicCodec: Codec[Topic]            = text
  val titleCodec: Codec[Title]            = text
  val channelCodec: Codec[SlackChannelId] = text.imap(SlackChannelId.apply)(_.value)
  val creatorCodec: Codec[SlackUserId]    = text.imap(SlackUserId.apply)(_.value)

  val taskCodec: Codec[Task] = (topicCodec ~ titleCodec ~ channelCodec ~ creatorCodec).gimap[Task]
}
