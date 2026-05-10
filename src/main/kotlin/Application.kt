package com.application

import com.application.features.groups.data.GroupRepository
import com.application.features.groups.domain.GroupService
import com.application.plugins.configureDatabase
import com.application.plugins.configureHTTP
import com.application.plugins.configureMonitoring
import com.application.plugins.configureRouting
import com.application.plugins.configureSecurity
import com.application.plugins.configureSerialization
import com.application.features.user.domain.UserService
import com.application.features.habits.data.HabitsRepository
import com.application.features.habits.domain.HabitsService
import com.application.features.user.data.UserRepository
import io.ktor.server.application.Application


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val userRepo = UserRepository()
    val habitRepo = HabitsRepository()
    val groupsRepo = GroupRepository();

    val userService = UserService(userRepo)
    val habitService = HabitsService(habitRepo, userRepo)
    val groupService = GroupService(groupsRepo, userRepo)

    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureDatabase()
    configureRouting(userService, habitService, groupService)
}
