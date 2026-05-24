package com.example.planwithfriends.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var isDarkTheme by mutableStateOf(false)
        private set

    fun toggleTheme(isDark: Boolean) {
        isDarkTheme = isDark
    }
}