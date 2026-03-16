package com.application.serializable

import com.application.storage.dao.HabitDAO
import com.application.storage.repository.dao.UserDAO
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID

data class User(
    val id: Int = 0,
    val username: String,
    val login: String,
    val passwordHash: String,
    val habits: List<Habit>
)

@Serializable
data class Habit(
    val id: Int,
    val habitName: String,
    val streak: Long
)

@Serializable
data class HabitAddRequest(
    val habitName: String,
    val login: String
)
