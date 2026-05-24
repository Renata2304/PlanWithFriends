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
import kotlinx.coroutines.launch
import java.time.LocalDate

// Starea ecranului de Calendar
data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val eventsForSelectedDate: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    // Starea internă (modificabilă doar de ViewModel)
    private val _uiState = MutableStateFlow(CalendarUiState())
    // Starea expusă către UI (Read-only)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // La inițializare, încărcăm evenimentele pentru ziua de azi
        loadEventsForDate(_uiState.value.selectedDate)
    }

    private fun loadEventsForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Formatăm data ca String pentru a o cere din Repository
            val dateString = date.toString()

            eventsRepository.getEventsForDate(dateString).collect { events ->
                _uiState.value = _uiState.value.copy(
                    eventsForSelectedDate = events,
                    isLoading = false
                )
            }
        }
    }

    // Funcție pe care o va apela UI-ul când utilizatorul dă click pe altă zi
    fun selectDate(newDate: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
        loadEventsForDate(newDate)
    }

    // Factory-ul necesar pentru a injecta Repository-ul din AppContainer (Ca în MarsPhotos)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PlanWithFriendsApplication)
                val eventsRepository = application.container.eventsRepository
                CalendarViewModel(eventsRepository = eventsRepository)
            }
        }
    }
}