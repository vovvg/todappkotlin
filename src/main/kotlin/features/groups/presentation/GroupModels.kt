package com.application.features.groups.presentation

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val creatorLogin: String
)

@Serializable
data class GroupResponse(
    val id: Int,
    val name: String,
    val members: List<GroupMemberResponse>
)

@Serializable
data class GroupMemberResponse(
    val id: Int,
    val username: String
)