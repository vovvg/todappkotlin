package com.application.storage.repository.dao

import com.application.serializable.User
import com.application.storage.dao.HabitDAO
import com.application.storage.dao.habitDaoToModel
import com.application.storage.tables.HabitTable
import com.application.storage.tables.UserTable
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

    val habits by HabitDAO.Companion referrersOn HabitTable.user
}

fun userDaoToModel(dao: UserDAO) = User(
    id = dao.id.value,
    username = dao.username,
    login = dao.login,
    passwordHash = dao.passwordHash,
    habits = dao.habits.map { habitDaoToModel(it) }
)

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)