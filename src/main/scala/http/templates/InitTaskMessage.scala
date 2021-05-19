package http.templates

import domain.task.{ TaskId, Title, Topic }
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.client.templating._
import org.latestbit.slack.morphism.common.{ SlackActionId, SlackResponseTypes, SlackUserId }
import org.latestbit.slack.morphism.messages.SlackBlock
object InitTaskMessage extends SlackBlocksTemplateDsl with SlackTextFormatters {
  def success(topic: Topic, title: Title, creator: SlackUserId, taskId: TaskId): Option[List[SlackBlock]] =
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

  val fail: SlackApiEventMessageReply = SlackApiEventMessageReply(
    text = "Invalid syntax",
    response_type = Some(SlackResponseTypes.Ephemeral)
  )
}
