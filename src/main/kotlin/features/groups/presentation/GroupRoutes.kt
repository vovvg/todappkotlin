package com.application.features.groups.presentation

import com.application.features.groups.domain.GroupService
import com.application.features.habits.domain.HabitsService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.groupRoutes(
    service: GroupService,
    habitsService: HabitsService,
) {

    route("/users") {

        get("/{login}/groups") {

            val login =
                call.parameters["login"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest
                    )

            val groups =
                service.getGroupsByUserLogin(login)

            call.respond(
                groups.map { it.toResponse() }
            )
        }
    }

    route("/groups") {

        get("/{groupId}") {

            val groupId =
                call.parameters["groupId"]
                    ?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest
                    )

            val viewerLogin =
                call.request.queryParameters["userLogin"]

            val group =
                service.getGroupById(groupId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound
                    )

            // If we know who's viewing, replace the habits list with one
            // that has streaks populated for that viewer.
            val habitsWithStreaks = viewerLogin?.let {
                habitsService.getGroupHabits(groupId, it)
            }

            call.respond(
                if (habitsWithStreaks != null) {
                    group.toResponse(habitsWithStreaks)
                } else {
                    group.toResponse()
                }
            )
        }

        post {

            val request =
                call.receive<CreateGroupRequest>()

            val group =
                service.createGroup(
                    name = request.name,
                    creatorLogin = request.creatorLogin
                )

            call.respond(
                HttpStatusCode.Created,
                group.toResponse()
            )
        }

        post("/{groupId}/members") {

            val groupId =
                call.parameters["groupId"]
                    ?.toIntOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest
                    )

            val request =
                call.receive<AddUserToGroupRequest>()

            val success =
                service.addUserToGroup(
                    request.login,
                    groupId
                )

            if (!success) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "User or group not found"
                )
            }

            call.respond(HttpStatusCode.OK)
        }

        delete("/{groupId}/members/{userId}") {

            val groupId =
                call.parameters["groupId"]
                    ?.toIntOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest
                    )

            val userId =
                call.parameters["userId"]
                    ?.toIntOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest
                    )

            val success =
                service.removeUserFromGroup(
                    userId,
                    groupId
                )

            if (!success) {
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    "Cannot remove user"
                )
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
