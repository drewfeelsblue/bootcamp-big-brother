package service

import cats.effect.{ Resource, Sync }
import cats.syntax.functor._
import cats.syntax.flatMap._
import domain.task.Task
import repository.task.TaskQueries
import skunk.Session

trait TaskService[F[_]] {
  def save(task: Task): F[Unit]
}

object TaskService {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): TaskService[F] = new TaskService[F] {
    override def save(task: Task): F[Unit] =
      (for {
        session <- sessionPool
        findPreparedQuery <- session.prepare(TaskQueries.findByTopicAndTitle)
        savePreparedQuery <- session.prepare(TaskQueries.save)
      } yield (findPreparedQuery, savePreparedQuery)).use {
        case (findPreparedQuery, savePreparedQuery) =>
          findPreparedQuery.option(task.topic, task.title) >>= {
              case Some(_) => Sync[F].pure(())
              case _       => savePreparedQuery.execute(task).void
            }
      }
  }
}
