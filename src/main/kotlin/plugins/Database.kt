package com.application.plugins

import com.application.database.DatabaseFactory
import io.ktor.server.application.Application

fun Application.configureDatabase() {
    DatabaseFactory.init(environment.config)
}