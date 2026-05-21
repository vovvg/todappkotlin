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

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.property("ktor.database.url").getString()
            driverClassName = config.property("ktor.database.driver").getString()
            username = config.property("ktor.database.user").getString()
            password = config.property("ktor.database.password").getString()

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

            // Track the day of the last check-in so we can enforce a
            // once-per-day cadence and detect broken streaks. Stored
            // as epoch-day (BIGINT) to avoid adding a date dependency.
            exec("ALTER TABLE habit_progress ADD COLUMN IF NOT EXISTS last_checkin_day BIGINT")
        }
    }
}