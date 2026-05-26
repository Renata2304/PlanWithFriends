package com.example.planwithfriends.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.planwithfriends.PlanWithFriendsApplication
import com.example.planwithfriends.data.Event
import com.example.planwithfriends.data.EventsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupDetailsViewModel(
    private val eventsRepository: EventsRepository,
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

    // AICI am mutat funcțiile tale!
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

    companion object {
        fun provideFactory(eventsRepository: EventsRepository, groupId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GroupDetailsViewModel(eventsRepository, groupId)
            }
        }
    }
}