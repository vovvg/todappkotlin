package com.application.storage.repository

import com.application.storage.model.User

interface UserRepository {
    fun allUser(): List<User>
    fun userByLogin(login: String): User?
    fun addUser(user: User)
    fun removeUser(login: String): Boolean
}

class FakeUserRepository : UserRepository {
    private val users = mutableListOf(
        User("cleaning", "test", "Priority.Low"),
        User("gardening", "Mow", "Priority.Medium"),
        User("shopping", "Buy the groceries", "Priority.High"),
        User("painting", "Paint the fence", "Priority.Medium")
    )

    override fun allUser(): List<User> = users

    override fun userByLogin(login: String): User? = users.find { it.login == login }

    override fun addUser(user: User) {
        if (userByLogin(user.login) == null)
            throw IllegalStateException("Cannot duplicate task names!")
        users.add(user)
    }

    override fun removeUser(login: String): Boolean {
        if (userByLogin(login) == null) {
            users.remove(userByLogin(login))
            return true
        }
        return false
    }

}