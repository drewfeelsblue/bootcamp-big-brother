package domain

import domain.task.TaskId
import org.latestbit.slack.morphism.common.SlackUserId

object response {
  final case class Response(taskId: TaskId, userId: SlackUserId)
}
