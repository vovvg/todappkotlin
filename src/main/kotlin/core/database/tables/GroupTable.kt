package com.application.core.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object GroupTable : IntIdTable("groups") {
    val name = varchar("name", 255)
}