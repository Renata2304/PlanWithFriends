package com.example.planwithfriends.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.planwithfriends.PlanWithFriendsApplication
import com.example.planwithfriends.data.Group
import com.example.planwithfriends.data.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Clasa de stare
data class GroupsUiState(
    val groupsList: List<Group> = emptyList(),
    val isLoading: Boolean = false
)

// 2. ViewModel-ul
class GroupsViewModel(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            groupsRepository.getAllGroups().collect { groups ->
                _uiState.value = _uiState.value.copy(
                    groupsList = groups,
                    isLoading = false
                )
            }
        }
    }

    // 3. FACTORY-ul (Foarte important! Aici îți dădea eroare dacă lipsea)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PlanWithFriendsApplication)
                val groupsRepository = application.container.groupsRepository
                GroupsViewModel(groupsRepository = groupsRepository)
            }
        }
    }
}