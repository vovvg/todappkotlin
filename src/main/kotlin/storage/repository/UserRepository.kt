package com.application.storage.repository

import com.application.storage.model.User

interface UserRepository {
    suspend fun allUser(): List<User>
    suspend fun userByLogin(login: String): User?
    suspend fun addUser(user: User)
    suspend fun removeUser(login: String): Boolean
}