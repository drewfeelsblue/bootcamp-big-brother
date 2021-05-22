package service

import cats.effect.{Resource, Sync}
import cats.syntax.flatMap._
import domain.task.{Task, TaskId}
import org.latestbit.slack.morphism.common.SlackChannelId
import service.queries.TaskQueries
import skunk.Session
import skunk.implicits.toIdOps

trait TaskService[F[_]] {
  def save(task: Task): F[TaskId]
  def findById(taskId: TaskId): F[Option[Task]]
  def countByChannel(channelId: SlackChannelId): F[Long]
}

object TaskService {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): TaskService[F] = new TaskService[F] {
    override def save(task: Task): F[TaskId] =
      (for {
        session           <- sessionPool
        findPreparedQuery <- session.prepare(TaskQueries.findByTopicAndTitleAndChannel)
        savePreparedQuery <- session.prepare(TaskQueries.save)
      } yield (findPreparedQuery, savePreparedQuery)).use { case (findPreparedQuery, savePreparedQuery) =>
        findPreparedQuery.option(task.topic ~ task.title ~ task.channelId) >>= {
          case Some((id, _)) => Sync[F].pure(id)
          case _             => savePreparedQuery.unique(task)
        }
      }

    override def findById(taskId: TaskId): F[Option[Task]] =
      sessionPool.flatMap(_.prepare(TaskQueries.findById)).use(_.option(taskId))

    override def countByChannel(channelId: SlackChannelId): F[Long] =
      sessionPool.flatMap(_.prepare(TaskQueries.countByChannel)).use(_.unique(channelId))
  }
}
