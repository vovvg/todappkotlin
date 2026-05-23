package com.application.features.user.domain

import com.application.features.habits.domain.Habit
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val username: String,
    val login: String,
    val passwordHash: String,
    val telegramId: Long? = null,
    val telegramUsername: String? = null,
    val habits: List<Habit>
)