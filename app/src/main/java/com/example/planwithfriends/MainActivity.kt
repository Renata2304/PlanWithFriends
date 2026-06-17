package com.example.planwithfriends

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.planwithfriends.ui.PlanWithFriendsApp
import com.example.planwithfriends.ui.screens.SettingsViewModel
import com.example.planwithfriends.ui.theme.PlanWithFriendsTheme
import com.example.planwithfriends.workers.EventNotificationWorker

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startEventPolling()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startEventPolling()
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            PlanWithFriendsTheme(
                darkTheme = settingsViewModel.isDarkTheme,
                season = settingsViewModel.currentSeasonTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PlanWithFriendsApp(settingsViewModel = settingsViewModel)
                }
            }
        }
    }

    private fun startEventPolling() {
        val workManager = WorkManager.getInstance(this)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<EventNotificationWorker>(
            15, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "EventPollingWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }
}