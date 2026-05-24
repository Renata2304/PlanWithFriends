package com.example.planwithfriends.data

interface AppContainer {
    val eventsRepository: EventsRepository
    val groupsRepository: GroupsRepository
}

class DefaultAppContainer : AppContainer {
    override val eventsRepository: EventsRepository by lazy {
        OfflineFirstEventsRepository()
    }

    override val groupsRepository: GroupsRepository by lazy {
        OfflineFirstGroupsRepository()
    }
}