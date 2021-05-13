import cats.effect.{ ExitCode, IO, IOApp }
import config.DbConfig
import skunk.codec.all.text
import skunk.implicits.toStringOps

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      dbConfig <- IO(DbConfig.load)
      _ <- IO(println(dbConfig))
      version <- AppResources.make[IO](dbConfig).map(_.psql).use { psql =>
                  psql.use(_.unique(sql"SELECT version();".query(text)))
                }
      _ <- IO(println(version))
    } yield ExitCode.Success
}
