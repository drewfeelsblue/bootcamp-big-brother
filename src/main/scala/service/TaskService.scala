package service

import cats.effect.{ Resource, Sync }
import cats.syntax.functor._
import domain.task.Task
import repository.task.TaskQueries
import skunk.Session

trait TaskService[F[_]] {
  def save(task: Task): F[Unit]
}

object TaskService {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): TaskService[F] = new TaskService[F] {
    override def save(task: Task): F[Unit] = sessionPool.flatMap(_.prepare(TaskQueries.save)).use(_.execute(task)).void
  }
}
