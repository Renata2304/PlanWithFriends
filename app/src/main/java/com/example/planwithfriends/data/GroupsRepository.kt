package com.example.planwithfriends.data

import com.example.planwithfriends.data.database.dao.GroupDao
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.network.NetworkGroup
import com.example.planwithfriends.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GroupsRepository {
    fun getAllGroups(): Flow<List<Group>>

    suspend fun createGroup(name: String, userId: String, userIcon: String)
    suspend fun joinGroup(groupId: String, userId: String, userIcon: String)

    suspend fun refreshMyGroups(userId: String)
    suspend fun updateMyIconInAllGroups(userId: String, newIcon: String)

    suspend fun syncGroups()
    suspend fun mergeOfflineDataWithServer(newUsername: String)
    suspend fun leaveGroup(groupId: String, userId: String, userIcon: String)
}

class OfflineFirstGroupsRepository(private val groupDao: GroupDao) : GroupsRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { entity ->
                Group(
                    id = entity.groupId,
                    name = entity.groupName,
                    memberCount = entity.memberCount,
                    memberIcons = entity.memberIcons,
                    members = entity.members
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

    override suspend fun refreshMyGroups(userId: String) {
        val oldGroups = groupDao.getAllGroupsOnce()
        oldGroups.forEach { groupDao.deleteGroupById(it.groupId) }

        val queryStr = "{\"members\":\"$userId\"}"
        val myGroupsOnServer = RetrofitClient.apiService.getGroupByCode(queryStr)

        myGroupsOnServer.forEach { netGroup ->
            val localEntity = GroupEntity(
                groupId = netGroup.groupId,
                groupName = netGroup.name,
                creatorId = userId,
                memberCount = netGroup.memberCount,
                memberIcons = netGroup.memberIcons ?: emptyList(),
                members = netGroup.members
            )
            groupDao.insertGroup(localEntity)
        }
    }

    override suspend fun syncGroups() {
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
                    memberCount = netGroup.memberCount,
                    memberIcons = netGroup.memberIcons ?: emptyList(),
                    members = netGroup.members
                )
                groupDao.insertGroup(updatedEntity)
            }
        }
    }

    override suspend fun updateMyIconInAllGroups(userId: String, newIcon: String) {
        val myLocalGroups = groupDao.getAllGroupsOnce()

        myLocalGroups.forEach { localGroup ->
            val queryStr = "{\"groupId\":\"${localGroup.groupId}\"}"
            val networkResult = RetrofitClient.apiService.getGroupByCode(queryStr)

            if (networkResult.isNotEmpty()) {
                val serverGroup = networkResult[0]
                val serverObjectId = serverGroup.id ?: return@forEach

                val membersList = serverGroup.members ?: emptyList()
                val iconsList = (serverGroup.memberIcons ?: emptyList()).toMutableList()

                val userIndex = membersList.indexOf(userId)

                if (userIndex != -1 && userIndex < iconsList.size) {
                    iconsList[userIndex] = newIcon

                    val updatedNetworkGroup = NetworkGroup(
                        groupId = serverGroup.groupId,
                        name = serverGroup.name,
                        memberCount = serverGroup.memberCount,
                        memberIcons = iconsList,
                        members = membersList
                    )

                    RetrofitClient.apiService.updateGroup(serverObjectId, updatedNetworkGroup)

                    val updatedEntity = GroupEntity(
                        groupId = serverGroup.groupId,
                        groupName = serverGroup.name,
                        creatorId = localGroup.creatorId,
                        memberCount = serverGroup.memberCount,
                        memberIcons = iconsList,
                        members = membersList
                    )
                    groupDao.insertGroup(updatedEntity)
                }
            }
        }
    }

    override suspend fun createGroup(name: String, userId: String, userIcon: String) {
        val shortGroupId = generateShortCode()

        val newNetworkGroup = NetworkGroup(
            groupId = shortGroupId,
            name = name,
            memberCount = 1,
            memberIcons = listOf(userIcon),
            members = listOf(userId)
        )

        RetrofitClient.apiService.createGroup(newNetworkGroup)

        val localEntity = GroupEntity(
            groupId = shortGroupId,
            groupName = name,
            creatorId = userId,
            memberCount = 1,
            memberIcons = listOf(userIcon),
            members = listOf(userId)
        )
        groupDao.insertGroup(localEntity)
    }

    override suspend fun mergeOfflineDataWithServer(newUsername: String) {
        val localGroups = groupDao.getAllGroupsOnce()

        localGroups.forEach { group ->
            if (group.creatorId == "my_user_id_123" || group.creatorId == "offline_user") {

                val queryStr = "{\"groupId\":\"${group.groupId}\"}"
                val existing = RetrofitClient.apiService.getGroupByCode(queryStr)

                if (existing.isEmpty()) {
                    val newNetworkGroup = NetworkGroup(
                        groupId = group.groupId,
                        name = group.groupName,
                        memberCount = group.memberCount,
                        memberIcons = group.memberIcons,
                        members = group.members
                    )
                    RetrofitClient.apiService.createGroup(newNetworkGroup)
                }

                val updatedEntity = GroupEntity(
                    groupId = group.groupId,
                    groupName = group.groupName,
                    creatorId = newUsername,
                    memberCount = group.memberCount,
                    memberIcons = group.memberIcons,
                    members = group.members
                )
                groupDao.insertGroup(updatedEntity)
            }
        }

        syncGroups()
    }

    override suspend fun joinGroup(groupId: String, userId: String, userIcon: String) {
        val queryStr = "{\"groupId\":\"$groupId\"}"
        val networkResult = RetrofitClient.apiService.getGroupByCode(queryStr)

        if (networkResult.isNotEmpty()) {
            val serverGroup = networkResult[0]
            val serverObjectId = serverGroup.id ?: return

            val noiiMembri = (serverGroup.members ?: emptyList()).toMutableList()

            if (noiiMembri.contains(userId)) return

            noiiMembri.add(userId)
            val noulNumarDeMembri = serverGroup.memberCount + 1

            val noileIconite = (serverGroup.memberIcons ?: emptyList()).toMutableList()
            noileIconite.add(userIcon)

            val updatedNetworkGroup = NetworkGroup(
                groupId = serverGroup.groupId,
                name = serverGroup.name,
                memberCount = noulNumarDeMembri,
                memberIcons = noileIconite,
                members = noiiMembri
            )
            RetrofitClient.apiService.updateGroup(serverObjectId, updatedNetworkGroup)

            val localEntity = GroupEntity(
                groupId = serverGroup.groupId,
                groupName = serverGroup.name,
                creatorId = userId,
                memberCount = noulNumarDeMembri,
                memberIcons = noileIconite,
                members = noiiMembri
            )
            groupDao.insertGroup(localEntity)
        }
    }

    override suspend fun leaveGroup(groupId: String, userId: String, userIcon: String) {
        groupDao.deleteGroupById(groupId)

        val queryStr = "{\"groupId\":\"$groupId\"}"
        val networkResult = RetrofitClient.apiService.getGroupByCode(queryStr)

        if (networkResult.isNotEmpty()) {
            val serverGroup = networkResult[0]
            val serverObjectId = serverGroup.id ?: return

            val noiiMembri = (serverGroup.members ?: emptyList()).toMutableList()
            noiiMembri.remove(userId)

            val noileIconite = (serverGroup.memberIcons ?: emptyList()).toMutableList()
            noileIconite.remove(userIcon)

            if (noiiMembri.isNotEmpty()) {
                val updatedGroup = NetworkGroup(
                    groupId = serverGroup.groupId,
                    name = serverGroup.name,
                    memberCount = noiiMembri.size,
                    memberIcons = noileIconite,
                    members = noiiMembri
                )
                RetrofitClient.apiService.updateGroup(serverObjectId, updatedGroup)
            } else {
                RetrofitClient.apiService.deleteGroup(serverObjectId)
            }
        }
    }
}