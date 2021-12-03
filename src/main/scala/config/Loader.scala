package config

import cats.effect.Sync
import cats.syntax.flatMap._
import com.typesafe.config.ConfigFactory
import org.typelevel.log4cats.Logger
import pureconfig.{ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object Loader {
  class PartiallyApplied[A <: DetailedToString](val dummy: Boolean = true) extends AnyVal {
    def apply[F[_]](path: String)(implicit
      logger: Logger[F],
      sync: Sync[F],
      configReader: ConfigReader[A],
      classTag: ClassTag[A]
    ): F[A] =
      sync
        .delay(ConfigSource.fromConfig(ConfigFactory.load().getConfig(path)).loadOrThrow[A])
        .flatTap(loaded => logger.info(s"$classTag loaded - $loaded"))
  }

  def apply[A <: DetailedToString] = new PartiallyApplied[A]
}
