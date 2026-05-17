package com.application.features.habits.domain

import com.application.features.groups.data.GroupRepository
import com.application.features.habits.data.HabitsRepository
import com.application.features.user.data.UserRepository

class HabitsService(
    private val habitRepo: HabitsRepository,
    private val userRepo: UserRepository,
) {

    suspend fun getUserHabits(login: String): List<Habit> {
        val user = userRepo.getByLogin(login) ?: return emptyList()
        return habitRepo.getUserHabits(user.id)
    }

    suspend fun getGroupHabits(groupId: Int) =
        habitRepo.getGroupHabits(groupId)

    suspend fun createUserHabit(
        login: String,
        habitName: String
    ) : Habit? {
        val user = userRepo.getByLogin(login) ?: return null
        return habitRepo.createForUser(habitName, user.id)
    }

    suspend fun createGroupHabit(
        groupId: Int,
        habitName: String
    ) =
        habitRepo.createForGroup(habitName, groupId)

    suspend fun deleteHabit(habitId: Int) =
        habitRepo.deleteHabit(habitId)
}