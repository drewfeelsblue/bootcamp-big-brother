package service

import cats.effect.{ Resource, Sync }
import cats.syntax.functor._
import model.Domain.Token
import org.latestbit.slack.morphism.common.SlackTeamId
import repository.token.TokenQueries
import skunk.Session

trait TokenService[F[_]] {
  def save(token: Token): F[Unit]
  def findByTeamId(teamId: SlackTeamId): F[Option[Token]]
}

object TokenService {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): TokenService[F] = new TokenService[F] {
    override def save(token: Token): F[Unit] = sessionPool.flatMap(_.prepare(TokenQueries.save)).use(_.execute(token)).void

    override def findByTeamId(teamId: SlackTeamId): F[Option[Token]] =
      sessionPool.flatMap(_.prepare(TokenQueries.findByTeamId)).use(_.stream(teamId, 64).compile.toList.map(_.lastOption))
  }
}