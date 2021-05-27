package service

import cats.Parallel
import cats.effect.{Concurrent, Resource, Sync, Timer}
import cats.implicits._
import com.evolutiongaming.scache.{Cache, ExpiringCache}
import domain.token.Token
import org.latestbit.slack.morphism.common.SlackTeamId
import service.queries.TokenQueries
import skunk.Session
import com.evolutiongaming.catshelper.Runtime
import com.evolutiongaming.scache.ExpiringCache.Config
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.{Duration, MINUTES}

trait TokenService[F[_]] {
  def save(token: Token): F[Unit]
  def findByTeamId(teamId: SlackTeamId): F[Option[Token]]
}

object TokenService {
  def make[F[_]: Concurrent: Timer: Runtime: Parallel: Logger](
    sessionPool: Resource[F, Session[F]]
  ): F[TokenService[F]] = {
    val config: Config[F, SlackTeamId, Token] = ExpiringCache.Config(Duration(5, MINUTES))
    Cache.expiring[F, SlackTeamId, Token](config).map { cache =>
      new TokenService[F] {
        override def save(token: Token): F[Unit] =
          (for {
            session           <- sessionPool
            findPreparedQuery <- session.prepare(TokenQueries.findByTeamId)
            savePreparedQuery <- session.prepare(TokenQueries.save)
          } yield (findPreparedQuery, savePreparedQuery)).use { case (findPreparedQuery, savePreparedQuery) =>
            findPreparedQuery.option(token.teamId) >>= {
              case Some(_) => Sync[F].pure(())
              case _       => savePreparedQuery.execute(token).void
            }
          }

        override def findByTeamId(teamId: SlackTeamId): F[Option[Token]] =
          cache.getOrUpdateOpt(teamId)(sessionPool.flatMap(_.prepare(TokenQueries.findByTeamId)).use(_.option(teamId)))
      }
    }
  }.use(_.pure[F])
}
