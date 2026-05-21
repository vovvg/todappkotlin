package com.application.features.groups.presentation

import com.application.features.groups.domain.Group
import com.application.features.habits.domain.Habit
import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val creatorLogin: String
)

@Serializable
data class AddUserToGroupRequest(
    val login: String
)

@Serializable
data class GroupResponse(
    val id: Int,
    val name: String,
    val members: List<GroupMemberResponse>,
    val habits: List<GroupHabitResponse>
)

@Serializable
data class GroupHabitResponse(
    val id: Int,
    val habitName: String,
    /** Streak for the viewer making the request. */
    val streak: Long = 0,
    /** Whether the viewer has already checked in today. */
    val checkedInToday: Boolean = false,
    /** All members of the group with their individual streaks. */
    val memberStreaks: List<MemberStreakResponse> = emptyList(),
)

@Serializable
data class MemberStreakResponse(
    val userId: Int,
    val login: String,
    val username: String,
    val streak: Long,
    val checkedInToday: Boolean,
)

@Serializable
data class GroupMemberResponse(
    val id: Int,
    val username: String,
    val login: String
)

fun Group.toResponse(habits: List<Habit> = this.habits) =
    GroupResponse(
        id = id,
        name = name,
        members = members.map {
            GroupMemberResponse(
                it.id,
                it.username,
                it.login
            )
        },
        habits = habits.map {
            GroupHabitResponse(
                id = it.id,
                habitName = it.habitName,
                streak = it.streak,
                checkedInToday = it.checkedInToday,
                memberStreaks = it.memberStreaks.map { ms ->
                    MemberStreakResponse(
                        userId = ms.userId,
                        login = ms.login,
                        username = ms.username,
                        streak = ms.streak,
                        checkedInToday = ms.checkedInToday,
                    )
                },
            )
        }
    )
