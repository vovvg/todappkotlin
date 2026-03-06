package com.application.storage.repository.local

import com.application.storage.model.User
import com.application.storage.repository.UserRepository
import kotlin.collections.remove

class FakeUserRepository : UserRepository {
    private val users = mutableListOf(
        User("cleaning", "test", "Priority.Low"),
        User("gardening", "Mow", "Priority.Medium"),
        User("shopping", "Buy the groceries", "Priority.High"),
        User("painting", "Paint the fence", "Priority.Medium")
    )

    override suspend fun allUser(): List<User> = users

    override suspend fun userByLogin(login: String): User? = users.find { it.login == login }

    override suspend fun addUser(user: User) {
        if (userByLogin(user.login) == null)
            throw IllegalStateException("Cannot duplicate task names!")
        users.add(user)
    }

    override suspend fun removeUser(login: String): Boolean {
        if (userByLogin(login) == null) {
            users.remove(userByLogin(login))
            return true
        }
        return false
    }

}