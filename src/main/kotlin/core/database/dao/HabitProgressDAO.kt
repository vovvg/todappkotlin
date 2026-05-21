package com.application.core.database.dao

import com.application.core.database.tables.HabitProgressTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class HabitProgressDAO(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<HabitProgressDAO>(HabitProgressTable)

    var habit by HabitDAO referencedOn HabitProgressTable.habit
    var user by UserDAO referencedOn HabitProgressTable.user
    var streak by HabitProgressTable.streak
    var lastCheckinDay by HabitProgressTable.lastCheckinDay
}
