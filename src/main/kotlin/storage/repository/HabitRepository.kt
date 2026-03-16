package com.application.storage.repository

import com.application.serializable.Habit
import com.application.storage.dao.HabitDAO
import com.application.storage.repository.dao.UserDAO
import com.application.storage.dao.habitDaoToModel
import com.application.storage.repository.dao.suspendTransaction

class HabitRepository {
    suspend fun addHabit(habitName: String, userId: Int): Habit = suspendTransaction {
        val user = UserDAO.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val dao = HabitDAO.new {
            this.habitName = habitName
            this.streak = 0
            this.user = user
        }

        habitDaoToModel(dao)
    }

    suspend fun getUserHabits(userId: Int): List<Habit> = suspendTransaction {
        val user = UserDAO.findById(userId) ?: return@suspendTransaction emptyList()
        user.habits.map(::habitDaoToModel)
    }
}