import cats.effect.{ Concurrent, ContextShift, Resource }
import config.DbConfig
import natchez.Trace.Implicits.noop
import skunk.Session

case class AppResources[F[_]](psql: Resource[F, Session[F]])

object AppResources {
  def make[F[_]: Concurrent: ContextShift](dbConfig: DbConfig): Resource[F, AppResources[F]] = {
    val DbConfig(dbHost, dbPort, dbUser, dbPassword, database, maxSessions) = dbConfig
    Session
      .pooled(
        dbHost,
        dbPort.value,
        dbUser.value,
        database.value,
        Some(dbPassword.value),
        maxSessions.value
      )
      .map(AppResources.apply)
  }
}
