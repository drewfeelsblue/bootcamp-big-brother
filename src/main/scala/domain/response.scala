package domain

import domain.task.TaskId
import io.estatico.newtype.macros.newtype
import org.latestbit.slack.morphism.common.SlackUserId
import scala.language.implicitConversions

object response {
  @newtype case class ReplyCount(value: Long)

  final case class Response(taskId: TaskId, userId: SlackUserId)
  final case class UserWithReplyCount(userId: SlackUserId, replyCount: ReplyCount)
}
