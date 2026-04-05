package com.application.core.database

import com.application.core.database.tables.HabitTable
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
            SchemaUtils.create(UserTable, HabitTable)
        }
    }
}