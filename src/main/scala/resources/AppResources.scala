package resources

import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, Resource}
import cats.implicits.catsSyntaxTuple2Semigroupal
import config.DbConfig
import natchez.Trace.Implicits.noop
import org.http4s.client.blaze.BlazeClientBuilder
import org.latestbit.slack.morphism.client.{SlackApiClient, SlackApiClientT}
import skunk.{Session, SessionPool}
import sttp.client.http4s.Http4sBackend

import scala.concurrent.ExecutionContext

case class AppResources[F[_]](psql: Resource[F, Session[F]], slackApiClient: SlackApiClientT[F])

object AppResources {
  def make[F[_]: ContextShift: ConcurrentEffect](dbConfig: DbConfig): Resource[F, AppResources[F]] = {
    val DbConfig(host, port, user, password, databaseName, maxSessions) = dbConfig
    val sessionPool: SessionPool[F] = Session
      .pooled(
        host.value,
        port.value,
        user.value,
        databaseName.value,
        Some(password.value.value),
        maxSessions.value
      )

    val slackApiClient: Resource[F, SlackApiClientT[F]] = for {
      client      <- BlazeClientBuilder[F](ExecutionContext.global).resource
      blocker     <- Blocker[F]
      slackClient <- SlackApiClient.build(Http4sBackend.usingClient(client, blocker)).resource()
    } yield slackClient

    (sessionPool, slackApiClient).mapN(AppResources.apply)
  }
}
