package com.application.plugins

import at.favre.lib.crypto.bcrypt.BCrypt
import com.application.serializable.HabitAddRequest
import com.application.serializable.LoginRequest
import com.application.serializable.LoginResponse
import com.application.serializable.RegisterRequest
import com.application.serializable.User
import com.application.storage.UsersRepository
import com.application.storage.repository.HabitRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(userRepository: UsersRepository, habitRepo: HabitRepository) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }

    }
    routing {

        staticResources("/", "static")


        post("/login") {
            val loginRequest = call.receive<LoginRequest>()

            val user = userRepository.userByLogin(loginRequest.login)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid login")

            val verified = BCrypt.verifyer().verify(
                loginRequest.password.toCharArray(),
                user.passwordHash
            ).verified

            if (!verified) return@post call.respond(HttpStatusCode.Unauthorized, "Invalid password")

            call.respond(LoginResponse(user.username, user.login))
        }

        post("/register") {
            val register = call.receive<RegisterRequest>()

            // Проверяем, нет ли такого логина
            if (userRepository.userByLogin(register.login) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Login already exists")
            }

            // Хешируем пароль
            val hash = BCrypt.withDefaults().hashToString(12, register.password.toCharArray())

            // Создаём нового пользователя через репозиторий
            val user = userRepository.addUser(
                User(
                    username = register.username,
                    login = register.login,
                    passwordHash = hash,
                    habits = emptyList()
                )
            )

            // Отправляем безопасный ответ на фронтенд
            call.respond(
                HttpStatusCode.Created,
                LoginResponse(
                    username = user.username,
                    login = user.login
                )
            )
        }

        route("/user") {

            get("/{login}/habits") {
                val login = call.parameters["login"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val user = userRepository.userByLogin(login)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

                call.respond(user.habits)
            }

            post("/habits/add") {
                val request = call.receive<HabitAddRequest>()
                val user = userRepository.userByLogin(request.login)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "User not found")

                val habit = habitRepo.addHabit(request.habitName, user.id)
                call.respond(HttpStatusCode.Created, habit)
            }
        }
    }
}
