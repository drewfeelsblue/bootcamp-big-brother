package migration

import cats.effect.Sync
import cats.implicits.toTraverseOps
import cats.syntax.flatMap._
import cats.syntax.functor._
import config.DbMigrationConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.typelevel.log4cats.Logger

import scala.jdk.CollectionConverters.ListHasAsScala

object DbMigration {
  def migrate[F[_]: Sync: Logger](dbMigrationConfig: DbMigrationConfig): F[Int] =
    for {
      fluentConfiguration <- Sync[F].delay {
                              val DbMigrationConfig(url, user, password) = dbMigrationConfig
                              Flyway.configure
                                .dataSource(url.value, user.value, password.value.value)
                                .group(true)
                                .outOfOrder(false)
                                .baselineOnMigrate(true)
                            }
      _ <- validate(fluentConfiguration)
      migrationCount <- Sync[F].delay(fluentConfiguration.load().migrate().migrationsExecuted)
    } yield migrationCount

  def validate[F[_]: Sync: Logger](configuration: FluentConfiguration): F[Unit] =
    for {
      validationResult <- Sync[F].delay(configuration.ignorePendingMigrations(true).load().validateWithResult())
      _ <- validationResult.invalidMigrations.asScala.toList.traverse(
            error => Logger[F].warn(s"""
             |Failed validation:
             |  - version: ${error.version}
             |  - path: ${error.filepath}
             |  - description: ${error.description}
             |  - errorCode: ${error.errorDetails.errorCode}
             |  - errorMessage: ${error.errorDetails.errorMessage}
             |""".stripMargin)
          )
    } yield ()
}
