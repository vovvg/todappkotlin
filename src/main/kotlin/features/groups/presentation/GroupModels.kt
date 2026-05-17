package com.application.features.groups.presentation

import com.application.features.groups.domain.Group
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
    val habitName: String
)

@Serializable
data class GroupMemberResponse(
    val id: Int,
    val username: String,
    val login: String
)
fun Group.toResponse() =
    GroupResponse(
        id=id,
        name=name,
        members=members.map {
            GroupMemberResponse(
                it.id,
                it.username,
                it.login
            )
        },
        habits=habits.map {
            GroupHabitResponse(
                it.id,
                it.habitName
            )
        }
    )