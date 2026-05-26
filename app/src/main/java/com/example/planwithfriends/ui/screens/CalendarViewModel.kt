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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val eventsForSelectedDate: List<Event> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val eventsRepository: EventsRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val _eventsForSelectedDate = _selectedDate.flatMapLatest { date ->
        eventsRepository.getEventsForDate(date.toString())
    }

    val uiState: StateFlow<CalendarUiState> = combine(_selectedDate, _eventsForSelectedDate) { date, events ->
        CalendarUiState(
            selectedDate = date,
            eventsForSelectedDate = events
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addEvent(title: String, time: String, date: LocalDate) {
        viewModelScope.launch {
            eventsRepository.insertEvent(title, time, date.toString())
        }
    }

    fun updateEvent(event: Event, newTitle: String, newTime: String) {
        viewModelScope.launch {
            eventsRepository.updateEvent(event.id, newTitle, newTime)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventsRepository.deleteEvent(event.id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PlanWithFriendsApplication)
                val eventsRepository = application.container.eventsRepository
                CalendarViewModel(eventsRepository)
            }
        }
    }
}