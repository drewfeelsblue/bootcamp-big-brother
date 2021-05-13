import cats.effect.{ Concurrent, ContextShift, Resource }
import config.DbConfig
import natchez.Trace.Implicits.noop
import skunk.Session

case class AppResources[F[_]](psql: Resource[F, Session[F]])

object AppResources {
  def make[F[_]: Concurrent: ContextShift](dbConfig: DbConfig): Resource[F, AppResources[F]] = {
    val DbConfig(host, port, user, password, databaseName, maxSessions) = dbConfig
    Session
      .pooled(
        host.value,
        port.value,
        user.value,
        databaseName.value,
        Some(password.value),
        maxSessions.value
      )
      .map(AppResources.apply)
  }
}
