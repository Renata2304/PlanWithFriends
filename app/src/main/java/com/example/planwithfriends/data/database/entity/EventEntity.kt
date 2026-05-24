package com.example.planwithfriends.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val eventId: String,
    val title: String,
    val time: String,
    val date: String,
    val creatorId: String,
    val groupId: String?
)