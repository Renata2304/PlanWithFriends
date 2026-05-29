package com.example.planwithfriends.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.planwithfriends.data.Event
import com.example.planwithfriends.data.EventsRepository
import com.example.planwithfriends.data.GroupsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupDetailsViewModel(
    private val eventsRepository: EventsRepository,
    private val groupsRepository: GroupsRepository,
    private val groupId: String
) : ViewModel() {

    val groupEvents: StateFlow<List<Event>> = eventsRepository.getEventsForGroup(groupId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshEvents()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            eventsRepository.syncEventsForGroup(groupId)
        }
    }

    fun addGroupEvent(title: String, time: String, date: String) {
        viewModelScope.launch {
            eventsRepository.addEventToGroupNetwork(title, time, date, groupId)
        }
    }

    fun updateGroupEvent(event: Event, newTitle: String, newTime: String) {
        viewModelScope.launch {
            eventsRepository.updateEvent(event.id, newTitle, newTime)
        }
    }

    fun deleteGroupEvent(event: Event) {
        viewModelScope.launch {
            eventsRepository.deleteEvent(event.id)
        }
    }

    fun leaveCurrentGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            groupsRepository.leaveGroup(groupId)
            onSuccess()
        }
    }

    companion object {
        fun provideFactory(
            eventsRepository: EventsRepository,
            groupsRepository: GroupsRepository,
            groupId: String
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GroupDetailsViewModel(eventsRepository, groupsRepository, groupId)
            }
        }
    }
}