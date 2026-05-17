package com.application.core.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object HabitProgressTable : IntIdTable("habit_progress") {

    val habit = reference(
        "habit_id",
        HabitTable,
        onDelete = ReferenceOption.CASCADE
    )

    val user = reference(
        "user_id",
        UserTable,
        onDelete = ReferenceOption.CASCADE
    )

    val streak = long("streak").default(0)
}