package com.example.planwithfriends.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.planwithfriends.data.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE date = :date")
    fun getEventsForDate(date: String): Flow<List<EventEntity>>

    @Query("UPDATE events SET title = :newTitle, time = :newTime WHERE eventId = :eventId")
    suspend fun updateEvent(eventId: String, newTitle: String, newTime: String)

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("SELECT * FROM events WHERE groupId = :groupId")
    fun getEventsForGroup(groupId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE eventId = :eventId LIMIT 1")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events")
    suspend fun getAllEventsOnce(): List<EventEntity>
}