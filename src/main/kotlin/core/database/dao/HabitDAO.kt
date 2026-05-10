package com.application.core.database.dao

import com.application.core.database.tables.HabitTable
import com.application.features.habits.domain.Habit
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class HabitDAO(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<HabitDAO>(HabitTable)

    var habitName by HabitTable.habitName
    var streak by HabitTable.streak

    var user by UserDAO referencedOn HabitTable.user
}

fun HabitDAO.toModel() = Habit(
    id = this.id.value,
    habitName = this.habitName,
    streak = this.streak
)