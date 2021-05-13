import cats.effect.{ ExitCode, IO, IOApp }
import config.DbConfig

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      dbConfig <- IO(DbConfig.load)
      _ <- IO(println(dbConfig))
    } yield ExitCode.Success
}
