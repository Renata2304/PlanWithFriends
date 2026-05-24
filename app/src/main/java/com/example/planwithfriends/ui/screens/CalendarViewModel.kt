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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val eventsForSelectedDate: List<Event> = emptyList()
)

class CalendarViewModel(private val eventsRepository: EventsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        fetchEventsForDate(LocalDate.now())
    }
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        fetchEventsForDate(date)
    }

    private fun fetchEventsForDate(date: LocalDate) {
        viewModelScope.launch {
            eventsRepository.getEventsForDate(date.toString()).collect { events ->
                _uiState.update { it.copy(eventsForSelectedDate = events) }
            }
        }
    }

    fun addEvent(title: String, time: String, date: LocalDate) {
        viewModelScope.launch {
            eventsRepository.insertEvent(title, time, date.toString())
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