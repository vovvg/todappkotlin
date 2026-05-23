package com.application.features.habits.data

import com.application.core.database.dao.GroupDAO
import com.application.core.database.dao.HabitDAO
import com.application.core.database.dao.HabitProgressDAO
import com.application.core.database.dao.UserDAO
import com.application.core.database.dao.suspendTransaction
import com.application.core.database.dao.toModel
import com.application.core.database.tables.HabitProgressTable
import com.application.features.habits.domain.Habit
import com.application.features.habits.domain.MemberStreak
import org.jetbrains.exposed.sql.and
import java.time.LocalDate

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

        val user = UserDAO.findById(userId)
            ?: return@suspendTransaction emptyList()

        val today = LocalDate.now().toEpochDay()

        user.habits.map { habit ->
            val progress = progressFor(habit.id.value, userId)
            Habit(
                id = habit.id.value,
                habitName = habit.habitName,
                streak = effectiveStreak(progress, today),
                checkedInToday = progress?.lastCheckinDay == today,
                memberStreaks = emptyList(),
            )
        }
    }

    suspend fun getGroupHabits(
        groupId: Int,
        viewerUserId: Int? = null,
    ): List<Habit> = suspendTransaction {

        val group = GroupDAO.findById(groupId)
            ?: return@suspendTransaction emptyList()

        val today = LocalDate.now().toEpochDay()
        // Snapshot members once so we can build the per-habit list cheaply.
        val members = group.members.toList()

        group.habits.map { habit ->
            val memberStreaks = members.map { member ->
                val progress = progressFor(habit.id.value, member.id.value)
                MemberStreak(
                    userId = member.id.value,
                    login = member.login,
                    username = member.username,
                    telegramUsername = member.telegramUsername,
                    streak = effectiveStreak(progress, today),
                    checkedInToday = progress?.lastCheckinDay == today,
                )
            }
            val viewer = viewerUserId
                ?.let { vid -> memberStreaks.firstOrNull { it.userId == vid } }

            Habit(
                id = habit.id.value,
                habitName = habit.habitName,
                streak = viewer?.streak ?: 0L,
                checkedInToday = viewer?.checkedInToday ?: false,
                memberStreaks = memberStreaks,
            )
        }
    }

    suspend fun deleteHabit(
        habitId: Int
    ): Boolean = suspendTransaction {

        val habit = HabitDAO.findById(habitId)
            ?: return@suspendTransaction false

        habit.delete()
        true
    }

    /**
     * Records a check-in for [userId] on [habitId] using once-per-day semantics:
     *   - same day                       → no-op, return current streak
     *   - exactly the next day           → streak += 1
     *   - more than 1 day gap, or never  → streak resets to 1
     *
     * Returns null if the habit or user does not exist.
     */
    suspend fun checkin(
        userId: Int,
        habitId: Int,
    ): Long? = suspendTransaction {

        val habit = HabitDAO.findById(habitId)
            ?: return@suspendTransaction null

        val user = UserDAO.findById(userId)
            ?: return@suspendTransaction null

        val today = LocalDate.now().toEpochDay()

        val existing = HabitProgressDAO.find {
            (HabitProgressTable.habit eq habit.id) and
                    (HabitProgressTable.user eq user.id)
        }.firstOrNull()

        if (existing == null) {
            val created = HabitProgressDAO.new {
                this.habit = habit
                this.user = user
                this.streak = 1L
                this.lastCheckinDay = today
            }
            return@suspendTransaction created.streak
        }

        val last = existing.lastCheckinDay
        when {
            last == today -> {
                // already checked in today — leave streak unchanged
            }
            last != null && today - last == 1L -> {
                existing.streak = existing.streak + 1
                existing.lastCheckinDay = today
            }
            else -> {
                // missed at least one day, or first check-in after a long gap
                existing.streak = 1L
                existing.lastCheckinDay = today
            }
        }
        existing.streak
    }

    /**
     * Loads (habit, user) progress within the surrounding transaction.
     */
    private fun progressFor(habitId: Int, userId: Int): HabitProgressDAO? {
        return HabitProgressDAO.find {
            (HabitProgressTable.habit eq habitId) and
                    (HabitProgressTable.user eq userId)
        }.firstOrNull()
    }

    /**
     * Returns the streak that should be displayed to a user: the stored
     * value if the streak is still active (checked in today or yesterday),
     * otherwise 0 because the streak has been broken.
     */
    private fun effectiveStreak(progress: HabitProgressDAO?, today: Long): Long {
        if (progress == null) return 0L
        val last = progress.lastCheckinDay ?: return 0L
        return if (today - last <= 1L) progress.streak else 0L
    }
}
