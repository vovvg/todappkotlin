package com.application.features.habits.presentation

import kotlinx.serialization.Serializable


@Serializable
data class HabitAddRequest(
    val habitName: String,
    val login: String
)
