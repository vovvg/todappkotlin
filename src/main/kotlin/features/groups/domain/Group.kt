package com.application.features.groups.domain

import com.application.features.habits.domain.Habit
import com.application.features.user.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val members: List<User>,
    val habits: List<Habit>
)