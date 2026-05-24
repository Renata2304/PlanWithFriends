package com.example.planwithfriends.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface EventsRepository {
    fun getEventsForDate(date: String): Flow<List<Event>>
}

class OfflineFirstEventsRepository : EventsRepository {
    override fun getEventsForDate(date: String): Flow<List<Event>> = flow {
        emit(
            listOf(
                Event("1", "Ședință proiect", "10:00 AM", date),
                Event("2", "Prânz cu grupul", "13:30 PM", date),
                Event("3", "Ieșire în oraș", "19:00 PM", date)
            )
        )
    }
}