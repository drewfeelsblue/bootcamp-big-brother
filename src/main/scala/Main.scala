import cats.effect.{ Concurrent, ExitCode, IO, IOApp }
import config.{ DbConfig, DbMigrationConfig, HttpServerConfig, Loader }
import http.routes.CommandRoutes
import migration.DbMigration
import org.http4s.HttpApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.middleware.{ RequestLogger, ResponseLogger }
import org.typelevel.log4cats.slf4j.Slf4jLogger
import eu.timepit.refined.pureconfig._
import pureconfig.generic.auto.exportReader
import resources.HttpServer

object Main extends IOApp {
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      httpServerConfig <- Loader[IO, HttpServerConfig]("http.server")
//      _ <- Loader[IO, DbConfig]("db")
      dbMigrationConfig <- Loader[IO, DbMigrationConfig]("db.migration")
      _ <- DbMigration.migrate[IO](dbMigrationConfig)
      _ <- HttpServer.resource(httpServerConfig, loggers[IO].apply(CommandRoutes[IO]().httpRoutes.orNotFound)).use(_ => IO.never)
    } yield ExitCode.Success

  def loggers[F[_]: Concurrent]: HttpApp[F] => HttpApp[F] = { http: HttpApp[F] =>
    RequestLogger.httpApp(true, true)(http)
  } andThen { http: HttpApp[F] =>
    ResponseLogger.httpApp(true, true)(http)
  }
}
