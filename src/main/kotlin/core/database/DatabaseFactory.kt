package com.application.core.database

import com.application.core.database.tables.GroupTable
import com.application.core.database.tables.HabitProgressTable
import com.application.core.database.tables.HabitTable
import com.application.core.database.tables.UserGroupTable
import com.application.core.database.tables.UserTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val (jdbcUrl, user, password) = resolveDatabaseConfig(config)

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            driverClassName = "org.postgresql.Driver"
            username = user
            this.password = password

            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.Companion.connect(dataSource)

        transaction {
            SchemaUtils.create(
                UserTable,
                HabitTable,
                GroupTable,
                UserGroupTable,
                HabitProgressTable,
            )

            // --- lightweight, idempotent migrations ---
            //
            // Earlier versions of the schema put `streak` on the `habits`
            // table as NOT NULL. Streak now lives on `habit_progress`
            // (per-user), so drop the legacy column if it survived from
            // an older deploy. CREATE TABLE IF NOT EXISTS won't touch
            // pre-existing columns, hence the explicit ALTER.
            exec("ALTER TABLE habits DROP COLUMN IF EXISTS streak")
            exec("ALTER TABLE habit_progress ADD COLUMN IF NOT EXISTS last_checkin_day BIGINT")
            exec("ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_id BIGINT UNIQUE")
            exec("ALTER TABLE groups ADD COLUMN IF NOT EXISTS owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL")
            exec("ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_username VARCHAR(50)")
        }
    }

    // Railway provides DATABASE_URL as postgresql://user:pass@host:port/db.
    // Fall back to individual config properties for local development.
    private fun resolveDatabaseConfig(config: ApplicationConfig): Triple<String, String, String> {
        val rawUrl = System.getenv("DATABASE_URL")
        if (rawUrl != null) {
            val uri = URI(rawUrl)
            val (user, pass) = (uri.userInfo ?: ":").split(":", limit = 2)
            val port = if (uri.port == -1) 5432 else uri.port
            val jdbcUrl = "jdbc:postgresql://${uri.host}:${port}${uri.path}"
            return Triple(jdbcUrl, user, pass)
        }
        return Triple(
            config.property("ktor.database.url").getString(),
            config.property("ktor.database.user").getString(),
            config.property("ktor.database.password").getString(),
        )
    }
}