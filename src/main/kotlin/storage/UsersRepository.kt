package com.application.storage

import com.application.serializable.User

//Local or postgre storage
interface UsersRepository {
    suspend fun allUser(): List<User>
    suspend fun userByLogin(login: String): User?
    suspend fun addUser(user: User) : User
    suspend fun removeUser(login: String): Boolean
}