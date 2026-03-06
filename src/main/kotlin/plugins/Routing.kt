package com.application.plugins

import com.application.storage.repository.UserRepository
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing


fun Application.configureRouting(repository: UserRepository) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }

    }
    install(ContentNegotiation) {
        json()
    }
    routing {

        staticResources("/static", "static")

        route("/users") {
            get {
                val users = repository.allUser()
                call.respond(users)
            }
            get("/byName/{userLogin}") {
                val name = call.parameters["userLogin"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val text = "<h1>Hello From Ktor ${repository.userByLogin(name)}</h1>"
                val type = ContentType.parse("text/html")
                call.respondText(text, type)
            }

        }
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
