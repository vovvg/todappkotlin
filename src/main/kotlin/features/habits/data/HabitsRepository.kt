package com.application.features.habits.data

import com.application.core.database.dao.HabitDAO
import com.application.core.database.dao.UserDAO
import com.application.core.database.dao.suspendTransaction
import com.application.core.database.dao.toModel
import com.application.features.habits.domain.Habit

class HabitsRepository {
    suspend fun addHabit(habitName: String, userId: Int): Habit = suspendTransaction {
        val user = UserDAO.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val dao = HabitDAO.Companion.new {
            this.habitName = habitName
            this.streak = 0
            this.user = user
        }

        dao.toModel()
    }

    suspend fun getUserHabits(userId: Int): List<Habit> = suspendTransaction {
        val user = UserDAO.findById(userId) ?: return@suspendTransaction emptyList()
        user.habits.map { it.toModel() }
    }

    suspend fun deleteHabit(habitId: Int, userId: Int): Boolean = suspendTransaction {
        val habit = HabitDAO.findById(habitId) ?: return@suspendTransaction false
        if (habit.user.id.value != userId) return@suspendTransaction false
        
        habit.delete()
        true
    }
}