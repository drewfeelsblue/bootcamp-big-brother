package service

import cats.effect.{Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import domain.response.Response
import service.queries.ResponseQueries
import skunk.Session

trait ResponseService[F[_]] {
  def save(response: Response): F[Unit]
}

object ResponseService {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): ResponseService[F] = new ResponseService[F] {
    override def save(response: Response): F[Unit] =
      (for {
        session           <- sessionPool
        findPreparedQuery <- session.prepare(ResponseQueries.findByTaskIdAndUserId)
        savePreparedQuery <- session.prepare(ResponseQueries.save)
      } yield (findPreparedQuery, savePreparedQuery)).use { case (findPreparedQuery, savePreparedQuery) =>
        findPreparedQuery.option(response.taskId, response.userId) >>= {
          case Some(_) => Sync[F].pure(())
          case _       => savePreparedQuery.execute(response).void
        }
      }
  }
}
