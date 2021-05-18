package repository.task

import domain.task.{ Task, Title, Topic }
import org.latestbit.slack.morphism.common.SlackUserId
import skunk.Codec
import skunk.codec.all.text
import repository.ext._

object TaskCodecs {
  val topicCodec: Codec[Topic]         = text
  val titleCodec: Codec[Title]         = text
  val creatorCodec: Codec[SlackUserId] = text.imap(SlackUserId.apply)(_.value)

  val taskCodec: Codec[Task] = (topicCodec ~ titleCodec ~ creatorCodec).gimap[Task]
}
