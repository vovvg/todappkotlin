package com.application.storage.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users") {
    val username = varchar("username", 50)
    val login = varchar("login", 50)
    val passwordHash = varchar("password_hash", 100)
}