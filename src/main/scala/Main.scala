import cats.effect.{ExitCode, IO, IOApp, Resource}
import config.AppConfig
import migration.DbMigration
import org.typelevel.log4cats.slf4j.Slf4jLogger

import modules.{HttpApi, Services}
import resources.{AppResources, HttpServer}

object Main extends IOApp {
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      appConfig                       <- Resource.eval(AppConfig.load[IO])
      _                               <- Resource.eval(DbMigration.migrate[IO](appConfig.dbMigrationConfig))
      AppResources(psql, slackClient) <- AppResources.make[IO](appConfig.dbConfig)
      services                        <- Services.resource(psql)
      routes                           = HttpApi.routes(services, slackClient, appConfig.slackAppConfig)
      server                          <- HttpServer.resource(appConfig.httpServerConfig, routes)
    } yield server).use(_ => IO.never)
}
