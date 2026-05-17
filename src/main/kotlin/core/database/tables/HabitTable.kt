package com.application.core.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object HabitTable : IntIdTable("habits") {

    val habitName = varchar("habit_name", 100)

    val ownerUser = reference(
        "owner_user_id",
        UserTable
    ).nullable()

    val ownerGroup = reference(
        "owner_group_id",
        GroupTable
    ).nullable()
}