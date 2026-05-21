package com.application.features.habits.presentation

import kotlinx.serialization.Serializable

@Serializable
data class HabitAddRequest(
    val habitName: String,
)

@Serializable
data class CheckinResponse(
    val habitId: Int,
    val streak: Long,
)
