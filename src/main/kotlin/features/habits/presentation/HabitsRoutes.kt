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

fun Route.habitRoutes(
    habitService: HabitsService
) {

    route("/habits") {

        delete("/{habitId}") {

            val habitId = call.parameters["habitId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            if (habitService.deleteHabit(habitId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    route("/users") {

        get("/{login}/habits") {

            val login = call.parameters["login"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            call.respond(
                habitService.getUserHabits(login)
            )
        }

        post("/{login}/habits") {

            val login = call.parameters["login"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<HabitAddRequest>()

            val habit = habitService.createUserHabit(
                login,
                request.habitName
            )

            call.respond(HttpStatusCode.Created, habit?: Unit)
        }
    }

    route("/groups") {

        get("/{groupId}/habits") {

            val groupId =
                call.parameters["groupId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

            call.respond(
                habitService.getGroupHabits(groupId)
            )
        }

        post("/{groupId}/habits") {

            val groupId =
                call.parameters["groupId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<HabitAddRequest>()

            val habit = habitService.createGroupHabit(
                groupId,
                request.habitName
            )

            call.respond(HttpStatusCode.Created, habit)
        }
    }
}