import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxTuple4Semigroupal
import config.{DbConfig, DbMigrationConfig, HttpServerConfig, Loader, SlackAppConfig}
import migration.DbMigration
import org.typelevel.log4cats.slf4j.Slf4jLogger
import eu.timepit.refined.pureconfig._
import modules.{HttpApi, Services}
import pureconfig.generic.auto.exportReader
import resources.{AppResources, HttpServer}

object Main extends IOApp {
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      (httpServerConfig, dbConfig, slackAppConfig, dbMigrationConfig) <- (
        Loader[IO, HttpServerConfig]("http.server"),
        Loader[IO, DbConfig]("db"),
        Loader[IO, SlackAppConfig]("slack"),
        Loader[IO, DbMigrationConfig]("db.migration")
      ).tupled
      _ <- DbMigration.migrate[IO](dbMigrationConfig)
      _ <- (for {
        AppResources(psql, slackClient) <- AppResources.make[IO](dbConfig)
        services                        <- Services.resource(psql)
        routes                           = HttpApi.routes(services, slackClient, slackAppConfig)
        server                          <- HttpServer.resource(httpServerConfig, routes)
      } yield server).use(_ => IO.never)
    } yield ExitCode.Success
}
