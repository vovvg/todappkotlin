package com.application.features.habits.domain

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: Int,
    val habitName: String,
    /** Viewer's current effective streak. 0 if no check-in or the streak is broken (>1 day since last check-in). */
    val streak: Long = 0,
    /** Whether the viewer has already checked in today. */
    val checkedInToday: Boolean = false,
    /** Per-member breakdown — populated for group habits only. */
    val memberStreaks: List<MemberStreak> = emptyList(),
)

@Serializable
data class MemberStreak(
    val userId: Int,
    val login: String,
    val username: String,
    val telegramUsername: String? = null,
    val streak: Long,
    val checkedInToday: Boolean,
)
