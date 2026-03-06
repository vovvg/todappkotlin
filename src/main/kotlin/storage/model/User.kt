package com.application.storage.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val login: String,
    val password: String
)