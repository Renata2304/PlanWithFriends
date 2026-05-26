package com.example.planwithfriends.data

import android.util.Log
import com.example.planwithfriends.data.database.dao.GroupDao
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.network.NetworkGroup
import com.example.planwithfriends.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface GroupsRepository {
    fun getAllGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String, userId: String)
    suspend fun joinGroup(groupId: String, userId: String)
    suspend fun syncGroups()
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
            val networkGroups = RetrofitClient.apiService.getAllGroups()
            networkGroups.forEach { netGroup ->
                val localEntity = GroupEntity(
                    groupId = netGroup.id,
                    groupName = netGroup.name,
                    creatorId = "synced_from_api",
                    memberCount = netGroup.memberCount
                )
                groupDao.insertGroup(localEntity)
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

            syncGroups()
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la crearea grupului: ${e.message}")
        }
    }

    override suspend fun joinGroup(groupId: String, userId: String) {
        syncGroups()
    }
}