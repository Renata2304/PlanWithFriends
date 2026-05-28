package com.example.planwithfriends.data

import android.util.Log
import com.example.planwithfriends.data.database.dao.GroupDao
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.network.NetworkGroup
import com.example.planwithfriends.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.collections.forEach

interface GroupsRepository {
    fun getAllGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String, userId: String)
    suspend fun joinGroup(groupId: String, userId: String)
    suspend fun syncGroups()
    suspend fun mergeOfflineDataWithServer(newUsername: String)
}

class OfflineFirstGroupsRepository(private val groupDao: GroupDao) : GroupsRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { entity ->
                Group(
                    id = entity.groupId,
                    name = entity.groupName,
                    memberCount = entity.memberCount
                )
            }
        }
    }

    private fun generateShortCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    override suspend fun syncGroups() {
        try {
            val localGroups = groupDao.getAllGroupsOnce()

            localGroups.forEach { localGroup ->
                val networkResult = RetrofitClient.apiService.getGroupByCode(localGroup.groupId)

                if (networkResult.isNotEmpty()) {
                    val netGroup = networkResult[0]
                    val updatedEntity = GroupEntity(
                        groupId = netGroup.id,
                        groupName = netGroup.name,
                        creatorId = localGroup.creatorId,
                        memberCount = netGroup.memberCount
                    )
                    groupDao.insertGroup(updatedEntity)
                }
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la sincronizarea grupurilor: ${e.message}")
        }
    }

    override suspend fun createGroup(name: String, userId: String) {
        try {
            val shortGroupId = generateShortCode()

            val newNetworkGroup = NetworkGroup(
                id = shortGroupId,
                name = name,
                memberCount = 1
            )

            RetrofitClient.apiService.createGroup(newNetworkGroup)

            val localEntity = GroupEntity(
                groupId = shortGroupId,
                groupName = name,
                creatorId = userId,
                memberCount = 1
            )
            groupDao.insertGroup(localEntity)

            syncGroups()
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la crearea grupului: ${e.message}")
        }
    }

    override suspend fun mergeOfflineDataWithServer(newUsername: String) {
        try {
            val localGroups = groupDao.getAllGroupsOnce()

            localGroups.forEach { group ->
                if (group.creatorId == "my_user_id_123" || group.creatorId == "offline_user") {

                    val newNetworkGroup = NetworkGroup(
                        id = group.groupId,
                        name = group.groupName,
                        memberCount = group.memberCount
                    )

                    RetrofitClient.apiService.createGroup(newNetworkGroup)

                    val updatedEntity = GroupEntity(
                        groupId = group.groupId,
                        groupName = group.groupName,
                        creatorId = newUsername,
                        memberCount = group.memberCount
                    )
                    groupDao.insertGroup(updatedEntity)
                }
            }

            syncGroups()

        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la migrarea datelor offline: ${e.message}")
        }
    }

    override suspend fun joinGroup(groupId: String, userId: String) {
        try {
            val networkResult = RetrofitClient.apiService.getGroupByCode(groupId)

            if (networkResult.isNotEmpty()) {
                val foundGroup = networkResult[0]

                val localEntity = GroupEntity(
                    groupId = foundGroup.id,
                    groupName = foundGroup.name,
                    creatorId = userId,
                    memberCount = foundGroup.memberCount + 1
                )
                groupDao.insertGroup(localEntity)
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la alăturarea în grup: ${e.message}")
        }
    }
}