package com.application.storage.repository

import com.application.serializable.User
import com.application.storage.UsersRepository
import com.application.storage.repository.dao.UserDAO
import com.application.storage.repository.dao.suspendTransaction
import com.application.storage.repository.dao.userDaoToModel
import com.application.storage.tables.UserTable


class UsersRepository : UsersRepository {
    override suspend fun allUser(): List<User> = suspendTransaction {
        UserDAO.all().map(::userDaoToModel)
    }

    override suspend fun userByLogin(login: String): User? = suspendTransaction {
        UserDAO.find { UserTable.login eq login }
            .limit(1)
            .map(::userDaoToModel)
            .firstOrNull()
    }

    override suspend fun addUser(user: User) = suspendTransaction {
        val dao = UserDAO.new {
            username = user.username
            login = user.login
            passwordHash = user.passwordHash
        }
        userDaoToModel(dao)
    }

    override suspend fun removeUser(login: String): Boolean {
        TODO("Not yet implemented")
    }
}