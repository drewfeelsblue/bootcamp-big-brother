import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits.catsSyntaxTuple2Semigroupal
import config.{ DbConfig, DbMigrationConfig, HttpServerConfig, Loader, SlackAppConfig }
import migration.DbMigration
import org.typelevel.log4cats.slf4j.Slf4jLogger
import eu.timepit.refined.pureconfig._
import modules.{ HttpApi, Services }
import pureconfig.generic.auto.exportReader
import resources.{ AppResources, HttpServer, SlackClient }

object Main extends IOApp {
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      httpServerConfig <- Loader[IO, HttpServerConfig]("http.server")
      dbConfig <- Loader[IO, DbConfig]("db")
      slackAppConfig <- Loader[IO, SlackAppConfig]("slack")
      dbMigrationConfig <- Loader[IO, DbMigrationConfig]("db.migration")
      _ <- DbMigration.migrate[IO](dbMigrationConfig)
      _ <- (AppResources.make[IO](dbConfig), SlackClient.resource[IO]).tupled.use {
            case (appResources, slackClient) =>
              val services = Services.make(appResources.psql)
              val httpApi  = HttpApi.make(services, slackClient, slackAppConfig)
              HttpServer.resource(httpServerConfig, httpApi.routes).use(_ => IO.never)
          }
    } yield ExitCode.Success
}
