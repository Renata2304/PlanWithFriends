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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groupsList: List<Group> = emptyList()
)

class GroupsViewModel(private val groupsRepository: GroupsRepository) : ViewModel() {

    private val currentUserId = "my_user_id_123"

    val uiState: StateFlow<GroupsUiState> = groupsRepository.getAllGroups()
        .map { groups -> GroupsUiState(groupsList = groups) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupsUiState()
        )

    init {
        viewModelScope.launch {
            groupsRepository.syncGroups()
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