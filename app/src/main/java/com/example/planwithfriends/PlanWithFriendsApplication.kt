package com.example.planwithfriends

import android.app.Application
import com.example.planwithfriends.data.AppContainer
import com.example.planwithfriends.data.DefaultAppContainer

class PlanWithFriendsApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}