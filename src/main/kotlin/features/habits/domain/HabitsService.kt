package com.application.features.habits.domain

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

    suspend fun getGroupHabits(
        groupId: Int,
        viewerLogin: String? = null,
    ): List<Habit> {
        val viewerId = viewerLogin
            ?.let { userRepo.getByLogin(it)?.id }
        return habitRepo.getGroupHabits(groupId, viewerId)
    }

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

    /**
     * Records a check-in for the given user on the given habit and returns
     * the new streak. Returns null if the user or habit doesn't exist.
     */
    suspend fun checkin(login: String, habitId: Int): Long? {
        val user = userRepo.getByLogin(login) ?: return null
        return habitRepo.checkin(user.id, habitId)
    }
}
