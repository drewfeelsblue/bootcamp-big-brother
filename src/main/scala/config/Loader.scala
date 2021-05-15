package config

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.typesafe.config.ConfigFactory
import org.typelevel.log4cats.Logger
import pureconfig.{ ConfigReader, ConfigSource }

import scala.reflect.ClassTag

object Loader {
  def apply[F[_]: Sync: Logger, A <: DetailedToString: ConfigReader](path: String)(implicit ct: ClassTag[A]): F[A] =
    for {
      config <- Sync[F].delay(ConfigFactory.load().getConfig(path))
      loaded <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[A])
      _ <- Logger[F].info(s"$ct loaded - $loaded")
    } yield loaded
}
