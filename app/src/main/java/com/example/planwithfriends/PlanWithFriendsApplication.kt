package com.example.planwithfriends

import android.app.Application
import com.example.planwithfriends.data.AppContainer
import com.example.planwithfriends.data.DefaultAppContainer

class PlanWithFriendsApplication : Application() {
    /** Instanța AppContainer folosită de restul claselor pentru a obține dependențele */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Aici se creează efectiv containerul când pornește aplicația
        container = DefaultAppContainer()
    }
}