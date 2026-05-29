package com.example.planwithfriends.data

import android.util.Log
import com.example.planwithfriends.data.database.dao.GroupDao
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.network.NetworkGroup
import com.example.planwithfriends.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GroupsRepository {
    fun getAllGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String, userId: String)
    suspend fun joinGroup(groupId: String, userId: String)
    suspend fun syncGroups()
    suspend fun mergeOfflineDataWithServer(newUsername: String)
    suspend fun leaveGroup(groupId: String)
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
                val queryStr = "{\"groupId\":\"${localGroup.groupId}\"}"
                val networkResult = RetrofitClient.apiService.getGroupByCode(queryStr)

                if (networkResult.isNotEmpty()) {
                    val netGroup = networkResult[0]
                    val updatedEntity = GroupEntity(
                        groupId = netGroup.groupId,
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
                groupId = shortGroupId,
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
                        groupId = group.groupId,
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
            val queryStr = "{\"groupId\":\"$groupId\"}"
            val networkResult = RetrofitClient.apiService.getGroupByCode(queryStr)

            if (networkResult.isNotEmpty()) {
                val serverGroup = networkResult[0]
                val serverObjectId = serverGroup.id ?: return

                val noulNumarDeMembri = serverGroup.memberCount + 1

                val localEntity = GroupEntity(
                    groupId = serverGroup.groupId,
                    groupName = serverGroup.name,
                    creatorId = userId,
                    memberCount = noulNumarDeMembri
                )
                groupDao.insertGroup(localEntity)

                val updatedNetworkGroup = NetworkGroup(
                    groupId = serverGroup.groupId,
                    name = serverGroup.name,
                    memberCount = noulNumarDeMembri
                )
                RetrofitClient.apiService.updateGroup(serverObjectId, updatedNetworkGroup)
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la alăturarea în grup: ${e.message}")
        }
    }

    override suspend fun leaveGroup(groupId: String) {
        try {
            groupDao.deleteGroupById(groupId)

            val queryStr = "{\"groupId\":\"$groupId\"}"
            val networkResult = RetrofitClient.apiService.getGroupByCode(queryStr)

            if (networkResult.isNotEmpty()) {
                val serverGroup = networkResult[0]

                val serverObjectId = serverGroup.id ?: return

                if (serverGroup.memberCount > 1) {
                    val updatedGroup = NetworkGroup(
                        groupId = serverGroup.groupId,
                        name = serverGroup.name,
                        memberCount = serverGroup.memberCount - 1
                    )
                    RetrofitClient.apiService.updateGroup(serverObjectId, updatedGroup)
                } else {
                    RetrofitClient.apiService.deleteGroup(serverObjectId)
                }
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la părăsirea grupului: ${e.message}")
        }
    }
}