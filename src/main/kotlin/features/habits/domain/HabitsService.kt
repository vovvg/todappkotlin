package com.application.features.habits.domain

import com.application.features.habits.data.HabitsRepository
import com.application.features.user.data.UserRepository

class HabitsService(
    private val habitsRepository: HabitsRepository,
    private val userRepository: UserRepository
) {

    suspend fun getHabitsByLogin(login: String): List<Habit>? {
        val user = userRepository.getByLogin(login) ?: return null
        return habitsRepository.getUserHabits(user.id)
    }

    suspend fun addHabit(login: String, habitName: String): Habit? {
        val user = userRepository.getByLogin(login) ?: return null
        return habitsRepository.addHabit(habitName, user.id)
    }
}