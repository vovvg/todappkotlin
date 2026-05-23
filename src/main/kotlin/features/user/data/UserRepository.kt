package com.application.features.user.data

import com.application.core.database.dao.UserDAO
import com.application.core.database.dao.suspendTransaction
import com.application.core.database.dao.toModel
import com.application.core.database.tables.UserTable
import com.application.features.user.domain.User
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or

class UserRepository {
    suspend fun allUser(): List<User> = suspendTransaction {
        UserDAO.all().map { it.toModel() }
    }

    suspend fun getByLogin(login: String): User? = suspendTransaction {
        UserDAO.find { UserTable.login eq login }
            .limit(1)
            .map { it.toModel() }
            .firstOrNull()
    }

    suspend fun getByTelegramId(telegramId: Long): User? = suspendTransaction {
        UserDAO.find { UserTable.telegramId eq telegramId }
            .limit(1)
            .map { it.toModel() }
            .firstOrNull()
    }

    suspend fun create(user: User) = suspendTransaction {
        val dao = UserDAO.new {
            username = user.username
            login = user.login
            passwordHash = user.passwordHash
            telegramId = user.telegramId
            telegramUsername = user.telegramUsername
        }
        dao.toModel()
    }

    suspend fun searchByUsername(query: String): List<User> = suspendTransaction {
        val q = "%${query.lowercase()}%"
        UserDAO.find {
            (UserTable.username.lowerCase() like q) or
            (UserTable.telegramUsername.lowerCase() like q)
        }
            .limit(8)
            .map { it.toModel() }
    }

    suspend fun remove(login: String): Boolean {
        TODO("Not yet implemented")
    }
}