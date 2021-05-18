package http.templates

import domain.task.{ Title, Topic }
import org.latestbit.slack.morphism.client.templating._
import org.latestbit.slack.morphism.common.{ SlackActionId, SlackUserId }
import org.latestbit.slack.morphism.messages.SlackBlock

class InitTaskResponse(topic: Topic, title: Title, creator: SlackUserId) extends SlackMessageTemplate {
  override def renderPlainText(): String = ""

  override def renderBlocks(): Option[List[SlackBlock]] =
    blocks(
      sectionBlock(
        text = pt"${topic.value}: ${title.value} task is initiated"
      ),
      actionsBlock(
        blockElements(
          button(text = pt"Reply", action_id = SlackActionId("reply_task_button"))
        )
      ),
      dividerBlock(),
      contextBlock(
        blockElements(
          md"by ${formatSlackUserId(creator)}"
        )
      )
    )
}
