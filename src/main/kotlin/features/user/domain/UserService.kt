package com.application.features.user.domain

import at.favre.lib.crypto.bcrypt.BCrypt
import com.application.features.user.data.UserRepository
import com.application.features.user.domain.User

class UserService(
    private val userRepository: UserRepository,
) {

    suspend fun login(login: String, password: String): User? {
        val user = userRepository.getByLogin(login) ?: return null

        val verified = BCrypt.verifyer().verify(
            password.toCharArray(),
            user.passwordHash
        ).verified

        return if (verified) user else null
    }

    suspend fun register(username: String, login: String, password: String): User? {
        if (userRepository.getByLogin(login) != null) return null

        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        return userRepository.create(
            User(
                username = username,
                login = login,
                passwordHash = hash,
                habits = emptyList()
            )
        )
    }
}