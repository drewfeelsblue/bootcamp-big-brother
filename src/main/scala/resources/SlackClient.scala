package resources

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift }
import org.http4s.client.blaze.BlazeClientBuilder
import org.latestbit.slack.morphism.client.SlackApiClient
import sttp.client.http4s.Http4sBackend

import scala.concurrent.ExecutionContext

object SlackClient {
  def resource[F[_]: ConcurrentEffect: ContextShift] =
    for {
      client <- BlazeClientBuilder[F](ExecutionContext.global).resource
      blocker <- Blocker[F]
      slackClient <- SlackApiClient.build(Http4sBackend.usingClient(client, blocker)).resource()
    } yield slackClient
}
