package com.example.planwithfriends.data

import com.example.planwithfriends.data.database.dao.GroupDao
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.database.entity.UserGroupCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface GroupsRepository {
    fun getAllGroups(): Flow<List<Group>>

    suspend fun createGroup(name: String, currentUserId: String)

    suspend fun joinGroup(groupId: String, currentUserId: String)
}

class OfflineFirstGroupsRepository(private val groupDao: GroupDao) : GroupsRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { entity ->
                Group(
                    id = entity.groupId,
                    name = entity.groupName,
                    memberCount = 0
                )
            }
        }
    }

    // 1. Creează un grup gol
    override suspend fun createGroup(name: String, currentUserId: String) {
        val newGroup = GroupEntity(
            groupId = UUID.randomUUID().toString(),
            groupName = name,
            creatorId = currentUserId
        )
        groupDao.insertGroup(newGroup)
    }

    override suspend fun joinGroup(groupId: String, currentUserId: String) {
        val crossRef = UserGroupCrossRef(
            userId = currentUserId,
            groupId = groupId
        )
        groupDao.insertUserGroupCrossRef(crossRef)
    }
}