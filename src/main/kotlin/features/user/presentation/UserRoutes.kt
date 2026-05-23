package com.application.features.user.presentation

import com.application.features.user.domain.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userRoutes(service: UserService, botToken: String, botUsername: String) {

    get("/config") {
        call.respond(BotConfigResponse(botUsername))
    }

    get("/user/search") {
        val query = call.request.queryParameters["query"]?.trim() ?: ""
        val users = service.searchUsers(query)
        call.respond(users.map { UserSearchResponse(it.login, it.username, it.telegramUsername) })
    }
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

    post("/auth/telegram") {
        val req = call.receive<TelegramAuthRequest>()

        val user = service.loginOrRegisterWithTelegram(req.initData, botToken)
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        call.respond(LoginResponse(user.username, user.login))
    }

    post("/auth/telegram/widget") {
        val req = call.receive<TelegramWidgetAuthRequest>()

        val user = service.loginOrRegisterWithTelegramWidget(req, botToken)
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        call.respond(LoginResponse(user.username, user.login))
    }
}