package com.application.features.habits.domain

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: Int,
    val habitName: String,
)