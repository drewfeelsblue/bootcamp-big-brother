import cats.effect.{ ExitCode, IO, IOApp }
import config.{ DbConfig, DbMigrationConfig }
import migration.DbMigration
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import skunk.codec.all.text
import skunk.implicits.toStringOps

object Main extends IOApp {
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      dbConfig <- DbConfig.load[IO]
      dbMigrationConfig <- DbMigrationConfig.load[IO]
      count <- DbMigration.migrate[IO](dbMigrationConfig)
      version <- AppResources.make[IO](dbConfig).map(_.psql).use { psql =>
                  psql.use(_.unique(sql"SELECT version();".query(text)))
                }
      _ <- Logger[IO].info(version)
    } yield ExitCode.Success
}
