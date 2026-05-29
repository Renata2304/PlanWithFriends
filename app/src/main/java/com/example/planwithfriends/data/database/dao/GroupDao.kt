package com.example.planwithfriends.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.database.entity.UserGroupCrossRef
import com.example.planwithfriends.data.network.NetworkGroup
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserGroupCrossRef(crossRef: UserGroupCrossRef)

    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups")
    suspend fun getAllGroupsOnce(): List<GroupEntity>

    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteGroupById(groupId: String)
}