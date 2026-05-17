package com.application.features.habits.data

import com.application.core.database.dao.GroupDAO
import com.application.core.database.dao.HabitDAO
import com.application.core.database.dao.UserDAO
import com.application.core.database.dao.suspendTransaction
import com.application.core.database.dao.toModel
import com.application.features.habits.domain.Habit

class HabitsRepository {

    suspend fun createForUser(
        habitName: String,
        userId: Int
    ): Habit = suspendTransaction {

        val user = UserDAO.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        HabitDAO.new {
            this.habitName = habitName
            ownerUser = user
            ownerGroup = null
        }.toModel()
    }

    suspend fun createForGroup(
        habitName: String,
        groupId: Int
    ): Habit = suspendTransaction {

        val group = GroupDAO.findById(groupId)
            ?: throw IllegalArgumentException("Group not found")

        HabitDAO.new {
            this.habitName = habitName
            ownerUser = null
            ownerGroup = group
        }.toModel()
    }

    suspend fun getUserHabits(
        userId: Int
    ): List<Habit> = suspendTransaction {

        UserDAO.findById(userId)
            ?.habits
            ?.map { it.toModel() }
            ?: emptyList()
    }

    suspend fun getGroupHabits(
        groupId: Int
    ): List<Habit> = suspendTransaction {

        GroupDAO.findById(groupId)
            ?.habits
            ?.map { it.toModel() }
            ?: emptyList()
    }

    suspend fun deleteHabit(
        habitId: Int
    ): Boolean = suspendTransaction {

        val habit = HabitDAO.findById(habitId)
            ?: return@suspendTransaction false

        habit.delete()
        true
    }
}