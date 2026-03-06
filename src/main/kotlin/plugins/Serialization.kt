package com.application.plugins

import com.application.storage.repository.UserRepository
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation


fun Application.configureSerialization(repository: UserRepository) {
    install(ContentNegotiation) {
        json()
    }
}
