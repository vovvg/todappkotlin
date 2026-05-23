package com.application.features.user.presentation

import kotlinx.serialization.SerialName
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

@Serializable
data class TelegramAuthRequest(
    val initData: String
)

// Data sent by the Telegram Login Widget (website button)
@Serializable
data class TelegramWidgetAuthRequest(
    val id: Long,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name")  val lastName: String?  = null,
    val username: String?  = null,
    @SerialName("photo_url") val photoUrl: String?   = null,
    @SerialName("auth_date") val authDate: Long,
    val hash: String,
)

@Serializable
data class BotConfigResponse(
    val botUsername: String,
)

@Serializable
data class UserSearchResponse(
    val login: String,
    val username: String,
)