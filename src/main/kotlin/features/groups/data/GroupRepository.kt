package com.application.features.groups.data

import com.application.core.database.dao.GroupDAO
import com.application.core.database.dao.HabitDAO
import com.application.core.database.dao.UserDAO
import com.application.core.database.dao.suspendTransaction
import com.application.core.database.dao.toModel
import com.application.core.database.tables.HabitTable
import com.application.features.groups.domain.Group
import org.jetbrains.exposed.sql.SizedCollection

class GroupRepository {

    suspend fun createGroup(name: String, creatorId: Int): Group = suspendTransaction {

        val creator = UserDAO.findById(creatorId)
            ?: throw IllegalArgumentException("Creator not found")

        val groupDAO = GroupDAO.new {
            this.name = name
            this.owner = creator
        }

        groupDAO.members = SizedCollection(listOf(creator))

        groupDAO.toModel()
    }

    suspend fun getAllByUser(userId: Int): List<Group> = suspendTransaction {

        val user = UserDAO.findById(userId)
            ?: return@suspendTransaction emptyList()

        user.groups.map { it.toModel() }
    }

    suspend fun getGroupById(groupId: Int): Group? = suspendTransaction {

        val groupDAO = GroupDAO.findById(groupId)
            ?: return@suspendTransaction null

        groupDAO.toModel()
    }

    suspend fun addUserToGroup(userId: Int, groupId: Int): Boolean = suspendTransaction {

        val user = UserDAO.findById(userId)
            ?: return@suspendTransaction false

        val group = GroupDAO.findById(groupId)
            ?: return@suspendTransaction false

        if (user in group.members) {
            return@suspendTransaction true
        }

        group.members = SizedCollection(group.members + user)

        true
    }

    suspend fun deleteGroup(groupId: Int, ownerLogin: String): Boolean = suspendTransaction {
        val group = GroupDAO.findById(groupId)
            ?: return@suspendTransaction false

        if (group.owner?.login != ownerLogin)
            return@suspendTransaction false

        // HabitTable.ownerGroup has no CASCADE, delete habits manually
        HabitDAO.find { HabitTable.ownerGroup eq group.id }
            .forEach { it.delete() }

        group.delete()
        true
    }

    suspend fun removeUserFromGroup(userId: Int, groupId: Int): Boolean = suspendTransaction {

        val user = UserDAO.findById(userId)
            ?: return@suspendTransaction false

        val group = GroupDAO.findById(groupId)
            ?: return@suspendTransaction false

        if (user !in group.members) {
            return@suspendTransaction false
        }

        if (group.members.count().toInt() == 1) {
            return@suspendTransaction false
        }

        group.members = SizedCollection(
            group.members.filter { it.id.value != userId }
        )

        true
    }

}