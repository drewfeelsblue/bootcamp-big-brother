package http.templates

import domain.response.UserWithReplyCount
import domain.task.{TaskCount, TaskId, Title, Topic}
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.client.templating._
import org.latestbit.slack.morphism.common.{SlackActionId, SlackResponseTypes, SlackUserId}
import org.latestbit.slack.morphism.messages.SlackBlock

object CommandMessage extends SlackBlocksTemplateDsl with SlackTextFormatters {
  def successInitTask(topic: Topic, title: Title, creator: SlackUserId, taskId: TaskId): Option[List[SlackBlock]] =
    blocks(
      sectionBlock(
        text = pt"${topic.value}: ${title.value} task is initiated"
      ),
      actionsBlock(
        blockElements(
          button(text = pt"Reply", action_id = SlackActionId(s"${taskId.value}"))
        )
      ),
      dividerBlock(),
      contextBlock(
        blockElements(
          md"by ${formatSlackUserId(creator)}"
        )
      )
    )

  val failInitTask: Option[List[SlackBlock]] =
    blocks(
      sectionBlock(
        text = pt"Invalid syntax"
      )
    )

  def report(totalTaskCount: TaskCount, usersWithReplyCount: List[UserWithReplyCount]): Option[List[SlackBlock]] =
    usersWithReplyCount
      .map(_ -> totalTaskCount)
      .map { case (UserWithReplyCount(userId, userReplyCount), totalTaskCount) =>
        sectionBlock(
          text = md"${formatSlackUserId(userId)} _$userReplyCount/${totalTaskCount}_"
        )
      }
}
