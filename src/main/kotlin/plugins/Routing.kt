package com.application.plugins

import com.application.features.groups.domain.GroupService
import com.application.features.groups.presentation.groupRoutes
import com.application.features.habits.domain.HabitsService
import com.application.features.habits.presentation.habitRoutes
import com.application.features.user.presentation.userRoutes
import com.application.features.user.domain.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(userService: UserService, habitsService: HabitsService, groupService: GroupService) {
    val botToken    = environment.config.property("telegram.botToken").getString()
    val botUsername = environment.config.propertyOrNull("telegram.botUsername")?.getString() ?: ""

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }

    }
    routing {
        staticResources("/", "static")

        userRoutes(userService, botToken, botUsername)
        habitRoutes(habitsService)
        groupRoutes(groupService, habitsService)
    }
}
