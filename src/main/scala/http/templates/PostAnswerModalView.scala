package http.templates

import org.latestbit.slack.morphism.client.templating.SlackModalViewTemplate
import org.latestbit.slack.morphism.common.SlackActionId
import org.latestbit.slack.morphism.messages.{ SlackBlock, SlackBlockPlainInputElement, SlackBlockPlainText }

class PostAnswerModalView extends SlackModalViewTemplate {
  override def titleText(): SlackBlockPlainText          = pt"Title"
  override def submitText(): Option[SlackBlockPlainText] = Some(pt"Submit")
  override def closeText(): Option[SlackBlockPlainText]  = Some(pt"Close")

  override def renderBlocks(): List[SlackBlock] =
    blocks(
      inputBlock(
        label = pt"Code",
        element = SlackBlockPlainInputElement(
          action_id = SlackActionId("on_enter_pressed")
        )
      )
    )
}
