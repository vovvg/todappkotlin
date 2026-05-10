package com.application.core.database.tables

import com.application.core.database.tables.GroupTable.reference
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserGroupTable : Table("user_groups") {

    val user = reference(
        "user_id",
        UserTable,
        onDelete = ReferenceOption.CASCADE
    )

    val group = reference(
        "group_id",
        GroupTable,
        onDelete = ReferenceOption.CASCADE
    )

    override val primaryKey = PrimaryKey(user, group)
}