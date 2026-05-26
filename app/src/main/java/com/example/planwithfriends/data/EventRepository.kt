package com.example.planwithfriends.data

import com.example.planwithfriends.data.database.dao.EventDao
import com.example.planwithfriends.data.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface EventsRepository {
    fun getEventsForDate(date: String): Flow<List<Event>>
    suspend fun insertEvent(title: String, time: String, date: String)
    suspend fun updateEvent(eventId: String, title: String, time: String)
    suspend fun deleteEvent(eventId: String)
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
}