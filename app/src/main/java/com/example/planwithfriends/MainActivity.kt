package com.example.planwithfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.planwithfriends.ui.PlanWithFriendsApp
import com.example.planwithfriends.ui.screens.SettingsViewModel
import com.example.planwithfriends.ui.theme.PlanWithFriendsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            PlanWithFriendsTheme(darkTheme = settingsViewModel.isDarkTheme,
                    season = settingsViewModel.currentSeasonTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PlanWithFriendsApp(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}