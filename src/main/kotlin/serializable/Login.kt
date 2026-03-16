package com.application.serializable

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val login: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val username: String,
    val login: String
)