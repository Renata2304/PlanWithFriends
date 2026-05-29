package com.example.planwithfriends.data

import android.util.Log
import com.example.planwithfriends.data.database.dao.EventDao
import com.example.planwithfriends.data.database.entity.EventEntity
import com.example.planwithfriends.data.network.NetworkGroupEvent // Asigură-te că și modelul ăsta are _id opțional
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

        val existingEvent = eventDao.getEventById(eventId)

        if (existingEvent != null && existingEvent.groupId != null) {
            val updatedNetworkEvent = NetworkGroupEvent(
                groupId = existingEvent.groupId,
                title = title,
                time = time,
                date = existingEvent.date
            )

            RetrofitClient.apiService.updateEvent(eventId, updatedNetworkEvent)
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        val existingEvent = eventDao.getEventById(eventId)

        eventDao.deleteEvent(eventId)

        if (existingEvent != null && existingEvent.groupId != null) {
            RetrofitClient.apiService.deleteEvent(eventId)
        }
    }

    override suspend fun syncEventsForGroup(groupId: String) {
        val queryStr = "{\"groupId\":\"$groupId\"}"
        val networkEvents = RetrofitClient.apiService.getEventsForGroup(queryStr)

        networkEvents.forEach { netEvent ->
            val localEntity = EventEntity(
                eventId = netEvent.id ?: UUID.randomUUID().toString(),
                title = netEvent.title,
                time = netEvent.time,
                date = netEvent.date,
                creatorId = "synced_user",
                groupId = netEvent.groupId
            )
            eventDao.insertEvent(localEntity)
        }
    }

    override suspend fun addEventToGroupNetwork(title: String, time: String, date: String, groupId: String) {
        val newNetworkEvent = NetworkGroupEvent(
            groupId = groupId,
            title = title,
            time = time,
            date = date
        )

        RetrofitClient.apiService.createGroupEvent(newNetworkEvent)

        syncEventsForGroup(groupId)
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