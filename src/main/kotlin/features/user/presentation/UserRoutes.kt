package com.application.features.user.presentation

import com.application.features.user.domain.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userRoutes(service: UserService) {
    route("/user") {
        post("/login") {
            val req = call.receive<LoginRequest>()

            val user = service.login(req.login, req.password)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            call.respond(LoginResponse(user.username, user.login))
        }

        post("/register") {
            val req = call.receive<RegisterRequest>()

            val user = service.register(req.username, req.login, req.password)
                ?: return@post call.respond(HttpStatusCode.Conflict)

            call.respond(LoginResponse(user.username, user.login))
        }
    }
}