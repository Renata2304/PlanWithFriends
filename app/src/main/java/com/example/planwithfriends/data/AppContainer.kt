package com.example.planwithfriends.data

import android.content.Context
import com.example.planwithfriends.data.database.AppDatabase

interface AppContainer {
    val eventsRepository: EventsRepository
    val groupsRepository: GroupsRepository
}
class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val eventsRepository: EventsRepository by lazy {
        OfflineFirstEventsRepository(database.eventDao())
    }

    override val groupsRepository: GroupsRepository by lazy {
        OfflineFirstGroupsRepository(database.groupDao())
    }
}