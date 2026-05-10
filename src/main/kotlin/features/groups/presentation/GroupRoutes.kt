package com.application.features.groups.presentation

import com.application.features.groups.domain.GroupService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.TypeInfo

fun Route.groupRoutes(service: GroupService) {

    route("/groups") {

        post("/create") {

            val request = call.receive<CreateGroupRequest>()

            service.createGroup(
                name = request.name,
                creatorLogin = request.creatorLogin
            )

            call.respond(HttpStatusCode.Created)
        }


        get("/getGroups/{userId}") {

            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val groups = service.getGroupsByUserId(userId)

            call.respond(groups)
        }

        get("/{id}") {

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val group = service.getGroupById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(group)
        }

        post("/{groupId}/add-user/{userLogin}") {

            val groupId = call.parameters["groupId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val userLogin = call.parameters["userLogin"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val success = service.addUserToGroup(userLogin, groupId)

            if (!success) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }

            call.respond(HttpStatusCode.OK)
        }

        delete("/{groupId}/remove-user/{userId}") {

            val groupId = call.parameters["groupId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val success = service.removeUserFromGroup(userId, groupId)

            if (!success) {
                return@delete call.respond(HttpStatusCode.BadRequest)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}