package config

import cats.effect.Sync
import cats.syntax.flatMap._
import com.typesafe.config.ConfigFactory
import org.typelevel.log4cats.Logger
import pureconfig.{ ConfigReader, ConfigSource }

import scala.reflect.ClassTag

object Loader {
  def apply[F[_]: Sync: Logger, A <: DetailedToString: ConfigReader](path: String)(implicit ct: ClassTag[A]): F[A] =
    Sync[F]
      .delay(ConfigSource.fromConfig(ConfigFactory.load().getConfig(path)).loadOrThrow[A])
      .flatTap(loaded => Logger[F].info(s"$ct loaded - $loaded"))
}
