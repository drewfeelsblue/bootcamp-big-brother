package migration

import cats.effect.Sync
import config.DbMigrationConfig
import eu.timepit.refined.types.string.NonEmptyString
import org.flywaydb.core.Flyway

object DbMigration {
  def migrate[F[_]: Sync](dbMigrationConfig: DbMigrationConfig): F[Int] = Sync[F].delay {
    val DbMigrationConfig(url, user, password) = dbMigrationConfig
    Flyway.configure
      .dataSource(url.value, user.value, password.value)
      .baselineOnMigrate(true)
      .load()
      .migrate()
      .migrationsExecuted
  }
}
