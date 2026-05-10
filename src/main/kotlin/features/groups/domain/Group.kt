package com.application.features.groups.domain

import com.application.features.user.domain.User

data class Group(
    val id: Int,
    val name: String,
    val members: List<User>
)