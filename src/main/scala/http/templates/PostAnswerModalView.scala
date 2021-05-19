package http.templates

import org.latestbit.slack.morphism.client.templating.SlackModalViewTemplate
import org.latestbit.slack.morphism.common.{ SlackActionId, SlackBlockId, SlackTriggerId }
import org.latestbit.slack.morphism.messages.{ SlackBlock, SlackBlockPlainInputElement, SlackBlockPlainText }

class PostAnswerModalView(blockId: String) extends SlackModalViewTemplate {
  override def titleText(): SlackBlockPlainText          = pt"Title"
  override def submitText(): Option[SlackBlockPlainText] = Some(pt"Submit")
  override def closeText(): Option[SlackBlockPlainText]  = Some(pt"Close")

  override def renderBlocks(): List[SlackBlock] =
    blocks(
      inputBlock(
        label = pt"Your code",
        element = SlackBlockPlainInputElement(
          action_id = SlackActionId("code"),
          multiline = Some(true)
        ),
        block_id = Some(SlackBlockId(blockId))
      )
    )
}
