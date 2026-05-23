package com.application.core.database.dao

import com.application.features.user.domain.User
import com.application.core.database.tables.HabitTable
import com.application.core.database.tables.UserGroupTable
import com.application.core.database.tables.UserTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserDAO(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<UserDAO>(UserTable)

    var username by UserTable.username
    var login by UserTable.login
    var passwordHash by UserTable.passwordHash
    var telegramId by UserTable.telegramId
    var telegramUsername by UserTable.telegramUsername

    val habits by HabitDAO optionalReferrersOn HabitTable.ownerUser
    var groups by GroupDAO via UserGroupTable
}

fun UserDAO.toModel() = User(
    id = this.id.value,
    username = this.username,
    login = this.login,
    passwordHash = this.passwordHash,
    telegramId = this.telegramId,
    telegramUsername = this.telegramUsername,
    habits = this.habits.map { it.toModel() }
)

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)