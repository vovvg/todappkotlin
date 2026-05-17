package com.application.core.database.dao

import com.application.core.database.tables.GroupTable
import com.application.core.database.tables.HabitTable
import com.application.core.database.tables.UserGroupTable
import com.application.features.groups.domain.Group
import com.application.features.habits.domain.Habit
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GroupDAO(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GroupDAO>(GroupTable)

    var name by GroupTable.name

    var members by UserDAO via UserGroupTable
    val habits by HabitDAO optionalReferrersOn HabitTable.ownerGroup
}

fun GroupDAO.toModel() = Group(
    id = this.id.value,
    name = this.name,
    members = this.members.map { it.toModel() },
    habits = this.habits.map { it.toModel() }
)