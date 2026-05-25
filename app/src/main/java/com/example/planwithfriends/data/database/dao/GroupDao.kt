package com.example.planwithfriends.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.database.entity.UserGroupCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserGroupCrossRef(crossRef: UserGroupCrossRef)

    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<GroupEntity>>
}