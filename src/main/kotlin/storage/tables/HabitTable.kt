package com.application.storage.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object HabitTable : IntIdTable("habits") {

    val habitName = varchar("habit_name", 100)
    val streak = long("streak")

    val user = reference(
        "user_id",
        UserTable,
        onDelete = ReferenceOption.CASCADE
    )
}