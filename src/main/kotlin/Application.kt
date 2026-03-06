package com.application

import com.application.plugins.configureDatabase
import com.application.plugins.configureHTTP
import com.application.plugins.configureMonitoring
import com.application.plugins.configureRouting
import com.application.plugins.configureSecurity
import com.application.plugins.configureSerialization
import com.application.storage.repository.local.FakeUserRepository
import com.application.storage.repository.postgres.PostgresUserRepository
import io.ktor.server.application.Application


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val userRepo = PostgresUserRepository()
    configureSerialization(userRepo)
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureDatabase()
    configureRouting(userRepo)
}
