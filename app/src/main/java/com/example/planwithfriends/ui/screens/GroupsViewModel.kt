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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groupsList: List<Group> = emptyList()
)

class GroupsViewModel(private val groupsRepository: GroupsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private val currentUserId = "my_user_id_123"

    init {
        fetchGroups()
    }

    private fun fetchGroups() {
        viewModelScope.launch {
            // .collect
            groupsRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(groupsList = groups) }
            }
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            groupsRepository.createGroup(name, currentUserId)
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            groupsRepository.joinGroup(groupId, currentUserId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PlanWithFriendsApplication)
                val groupsRepository = application.container.groupsRepository
                GroupsViewModel(groupsRepository)
            }
        }
    }
}