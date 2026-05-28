package com.example.planwithfriends.ui.screens

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.core.content.edit
import com.example.planwithfriends.PlanWithFriendsApplication
import com.example.planwithfriends.R // Asigură-te că acest import există!
import com.example.planwithfriends.data.GroupsRepository
import com.example.planwithfriends.data.network.NetworkUser
import com.example.planwithfriends.data.network.RetrofitClient
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val groupsRepository: GroupsRepository
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var isDarkTheme by mutableStateOf(sharedPreferences.getBoolean("is_dark", false))
        private set
    var currentSeasonTheme by mutableStateOf(sharedPreferences.getString("season", "auto") ?: "auto")
        private set
    var currentLanguage by mutableStateOf(sharedPreferences.getString("language", "English") ?: "English")
        private set

    var currentUser by mutableStateOf(sharedPreferences.getString("current_user", null))
        private set

    var authErrorMessage by mutableStateOf<String?>(null)
        private set
    var isAuthLoading by mutableStateOf(false)
        private set

    fun toggleTheme(isDark: Boolean) {
        isDarkTheme = isDark
        sharedPreferences.edit {putBoolean("is_dark", isDark)}
    }
    fun setSeasonTheme(theme: String) {
        currentSeasonTheme = theme
        sharedPreferences.edit { putString("season", theme) }
    }
    fun setLanguage(language: String) {
        currentLanguage = language
        sharedPreferences.edit { putString("language", language) }
        val languageCode = when (language) { "Română" -> "ro"; "Français" -> "fr"; "Español" -> "es"; else -> "en" }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }

    fun login(username: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading = true
            authErrorMessage = null
            try {
                val queryStr = "{\"username\":\"$username\"}"
                val users = RetrofitClient.apiService.getUserByUsername(queryStr)

                if (users.isEmpty()) {
                    authErrorMessage = getApplication<Application>().getString(R.string.error_user_not_found)
                } else if (users[0].password != pass) {
                    authErrorMessage = getApplication<Application>().getString(R.string.error_incorrect_password)
                } else {
                    finalizeAuth(username)
                    onSuccess()
                }
            } catch (e: Exception) {
                authErrorMessage = getApplication<Application>().getString(R.string.error_general, e.message)
            } finally {
                isAuthLoading = false
            }
        }
    }

    fun register(username: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading = true
            authErrorMessage = null
            try {
                val queryStr = "{\"username\":\"$username\"}"
                val existingUsers = RetrofitClient.apiService.getUserByUsername(queryStr)

                if (existingUsers.isNotEmpty()) {
                    authErrorMessage = getApplication<Application>().getString(R.string.error_username_taken)
                } else {
                    val newUser = NetworkUser(username = username, password = pass)
                    RetrofitClient.apiService.createUser(newUser)

                    finalizeAuth(username)
                    onSuccess()
                }
            } catch (e: Exception) {
                authErrorMessage = getApplication<Application>().getString(R.string.error_general, e.message)
            } finally {
                isAuthLoading = false
            }
        }
    }

    private fun finalizeAuth(username: String) {
        currentUser = username
        sharedPreferences.edit { putString("current_user", username) }
        viewModelScope.launch {
            groupsRepository.mergeOfflineDataWithServer(username)
        }
    }

    fun clearAuthError() { authErrorMessage = null }

    fun logout() {
        currentUser = null
        sharedPreferences.edit { remove("current_user") }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PlanWithFriendsApplication)
                SettingsViewModel(application, application.container.groupsRepository)
            }
        }
    }
}