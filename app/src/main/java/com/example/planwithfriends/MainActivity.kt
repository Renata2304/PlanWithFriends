package com.example.planwithfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.planwithfriends.ui.PlanWithFriendsApp
import com.example.planwithfriends.ui.theme.PlanWithFriendsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PlanWithFriendsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PlanWithFriendsApp()
                }
            }
        }
    }
}