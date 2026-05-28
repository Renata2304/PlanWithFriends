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
import com.example.planwithfriends.data.GroupsRepository
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

        val languageCode = when (language) {
            "Română" -> "ro"
            "Français" -> "fr"
            "Español" -> "es"
            else -> "en"
        }

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }

    fun login(username: String) {
        currentUser = username
        sharedPreferences.edit { putString("current_user", username) }

        viewModelScope.launch {
            groupsRepository.mergeOfflineDataWithServer(username)
        }
    }

    fun logout() {
        currentUser = null
        sharedPreferences.edit { remove("current_user") }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PlanWithFriendsApplication)
                val groupsRepository = application.container.groupsRepository
                SettingsViewModel(application, groupsRepository)
            }
        }
    }
}