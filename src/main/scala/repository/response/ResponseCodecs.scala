package repository.response

import domain.response.Response
import domain.task.TaskId
import org.latestbit.slack.morphism.common.SlackUserId
import skunk.Codec
import skunk.codec.all.{int8, text}
import repository.ext._

object ResponseCodecs {
  val taskIdCodec: Codec[TaskId]      = int8
  val userIdCodec: Codec[SlackUserId] = text.imap(SlackUserId.apply)(_.value)

  val responseCodec: Codec[Response] = (taskIdCodec ~ userIdCodec).gimap[Response]
}
