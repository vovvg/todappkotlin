package com.application.features.user.domain

import com.application.features.habits.domain.Habit

data class User(
    val id: Int = 0,
    val username: String,
    val login: String,
    val passwordHash: String,
    val habits: List<Habit>
)