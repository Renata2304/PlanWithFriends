package com.example.planwithfriends.data

import android.util.Log
import com.example.planwithfriends.data.database.dao.EventDao
import com.example.planwithfriends.data.database.entity.EventEntity
import com.example.planwithfriends.data.network.NetworkGroupEvent
import com.example.planwithfriends.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface EventsRepository {
    fun getEventsForDate(date: String): Flow<List<Event>>
    suspend fun insertEvent(title: String, time: String, date: String)
    suspend fun updateEvent(eventId: String, title: String, time: String)
    suspend fun deleteEvent(eventId: String)
    suspend fun syncEventsForGroup(groupId: String)
    suspend fun addEventToGroupNetwork(title: String, time: String, date: String, groupId: String)
    fun getEventsForGroup(groupId: String): Flow<List<Event>>
}

class OfflineFirstEventsRepository(private val eventDao: EventDao) : EventsRepository {

    override fun getEventsForDate(date: String): Flow<List<Event>> {
        return eventDao.getEventsForDate(date).map { entities ->
            entities.map { entity ->
                Event(
                    id = entity.eventId,
                    title = entity.title,
                    time = entity.time,
                    date = entity.date
                )
            }
        }
    }

    override suspend fun insertEvent(title: String, time: String, date: String) {
        val newEntity = EventEntity(
            eventId = UUID.randomUUID().toString(),
            title = title,
            time = time,
            date = date,
            creatorId = "current_user_id",
            groupId = null
        )
        eventDao.insertEvent(newEntity)
    }

    override suspend fun updateEvent(eventId: String, title: String, time: String) {
        eventDao.updateEvent(eventId, title, time)
    }

    override suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEvent(eventId)
    }

    override suspend fun syncEventsForGroup(groupId: String) {
        try {
            val networkEvents = RetrofitClient.apiService.getEventsForGroup(groupId)

            networkEvents.forEach { netEvent ->
                val localEntity = EventEntity(
                    eventId = netEvent.id,
                    title = netEvent.title,
                    time = netEvent.time,
                    date = netEvent.date,
                    creatorId = "synced_user",
                    groupId = netEvent.groupId
                )
                eventDao.insertEvent(localEntity)
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la sincronizare: ${e.message}")
        }
    }

    override suspend fun addEventToGroupNetwork(title: String, time: String, date: String, groupId: String) {
        try {
            val newNetworkEvent = NetworkGroupEvent(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                title = title,
                time = time,
                date = date
            )

            RetrofitClient.apiService.createGroupEvent(newNetworkEvent)

            syncEventsForGroup(groupId)

        } catch (e: Exception) {
            Log.e("API_SYNC", "Eroare la trimiterea evenimentului: ${e.message}")
        }
    }

    override fun getEventsForGroup(groupId: String): Flow<List<Event>> {
        return eventDao.getEventsForGroup(groupId).map { entities ->
            entities.map { entity ->
                Event(
                    id = entity.eventId,
                    title = entity.title,
                    time = entity.time,
                    date = entity.date
                )
            }
        }
    }
}