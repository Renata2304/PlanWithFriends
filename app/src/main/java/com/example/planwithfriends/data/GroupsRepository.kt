package com.example.planwithfriends.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface GroupsRepository {
    fun getAllGroups(): Flow<List<Group>>
}

class OfflineFirstGroupsRepository : GroupsRepository {
    override fun getAllGroups(): Flow<List<Group>> = flow {
        emit(
            listOf(
                Group("1", "Group 1", 4),
                Group("2", "Group 2", 3),
                Group("3", "Group 3", 5)
            )
        )
    }
}