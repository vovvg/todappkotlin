package com.application.features.groups.domain

import com.application.features.groups.data.GroupRepository
import com.application.features.user.data.UserRepository

class GroupService(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    suspend fun createGroup(name: String, creatorLogin: String): Group? {
        val user = userRepository.getByLogin(creatorLogin) ?: return null
        return groupRepository.createGroup(name, user.id)
    }

    suspend fun getGroupsByUserId(userId: Int): List<Group> {
        return groupRepository.getAllByUser(userId).toList()
    }

    suspend fun getGroupById(groupId: Int): Group? {
        return groupRepository.getGroupById(groupId)
    }

    suspend fun addUserToGroup(userLogin: String, groupId: Int): Boolean {
        val user = userRepository.getByLogin(userLogin) ?: return false
        return groupRepository.addUserToGroup(user.id, groupId)
    }

    suspend fun removeUserFromGroup(userId: Int, groupId: Int): Boolean {
//        val user = userRepository.getByLogin(userLogin) ?: return false
        return groupRepository.removeUserFromGroup(userId, groupId)
    }
}