package com.application.features.user.data

import com.application.features.user.domain.User
import com.application.core.database.dao.UserDAO
import com.application.core.database.dao.suspendTransaction
import com.application.core.database.dao.userDaoToModel
import com.application.core.database.tables.UserTable

class UserRepository {
    suspend fun allUser(): List<User> = suspendTransaction {
        UserDAO.all().map(::userDaoToModel)
    }

    suspend fun getByLogin(login: String): User? = suspendTransaction {
        UserDAO.Companion.find { UserTable.login eq login }
            .limit(1)
            .map(::userDaoToModel)
            .firstOrNull()
    }

    suspend fun create(user: User) = suspendTransaction {
        val dao = UserDAO.new {
            username = user.username
            login = user.login
            passwordHash = user.passwordHash
        }
        userDaoToModel(dao)
    }

    suspend fun remove(login: String): Boolean {
        TODO("Not yet implemented")
    }
}