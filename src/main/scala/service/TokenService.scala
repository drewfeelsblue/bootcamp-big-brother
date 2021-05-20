package service

import cats.effect.{Resource, Sync}
import cats.syntax.functor._
import cats.syntax.flatMap._
import domain.token.Token
import org.latestbit.slack.morphism.common.SlackTeamId
import repository.TokenQueries
import skunk.Session

trait TokenService[F[_]] {
  def save(token: Token): F[Unit]
  def findByTeamId(teamId: SlackTeamId): F[Option[Token]]
}

object TokenService {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): TokenService[F] = new TokenService[F] {
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
      sessionPool.flatMap(_.prepare(TokenQueries.findByTeamId)).use(_.option(teamId))
  }
}
