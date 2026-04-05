package com.application.features.habits.presentation

import kotlinx.serialization.Serializable


@Serializable
data class HabitAddRequest(
    val habitName: String,
    val login: String
)

@Serializable
data class HabitDeleteRequest(
    val login: String,
    val habitId: Int
)