package com.application.core.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GroupTable : IntIdTable("groups") {
    val name = varchar("name", 255)
    val ownerId = reference("owner_id", UserTable, onDelete = ReferenceOption.SET_NULL).nullable()
}