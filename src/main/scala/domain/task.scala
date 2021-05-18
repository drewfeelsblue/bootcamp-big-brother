package domain

import io.estatico.newtype.macros.newtype
import org.latestbit.slack.morphism.common.{ SlackChannelId, SlackUserId }

object task {

  @newtype case class Topic(value: String)
  @newtype case class Title(value: String)

  final case class Task(
      topic: Topic,
      title: Title,
      channelId: SlackChannelId,
      creatorId: SlackUserId
  )
}
