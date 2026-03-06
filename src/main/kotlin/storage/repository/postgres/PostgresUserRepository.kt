package com.application.storage.repository.postgres

import com.application.storage.model.User
import com.application.storage.repository.UserRepository
import com.application.storage.repository.db.UserDAO
import com.application.storage.repository.db.UserTable
import com.application.storage.repository.db.daoToModel
import com.application.storage.repository.db.suspendTransaction

class PostgresUserRepository : UserRepository {
    override suspend fun allUser(): List<User> = suspendTransaction {
        UserDAO.all().map(::daoToModel)
    }

    override suspend fun userByLogin(login: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun addUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUser(login: String): Boolean {
        TODO("Not yet implemented")
    }
}