package com.application.features.habits.presentation

import com.application.features.habits.domain.HabitsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.habitRoutes(habitsService: HabitsService) {
    route("/habits") {

        get("/{login}") {
            val login = call.parameters["login"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val habits = habitsService.getHabitsByLogin(login)
                ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

            call.respond(habits)
        }

        post("/add") {
            val request = call.receive<HabitAddRequest>()
            val habit = habitsService.addHabit(request.login, request.habitName)
                ?: return@post call.respond(HttpStatusCode.BadRequest, "User not found")

            call.respond(HttpStatusCode.Created, habit)
        }
        
        delete("/delete") {
            val request = call.receive<HabitDeleteRequest>()
            
            if (habitsService.deleteHabit(request.login, request.habitId)) {
                call.respond(HttpStatusCode.OK, "Habit deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Habit not found or access denied")
            }
        }
    }
}