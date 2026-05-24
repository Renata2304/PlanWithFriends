package com.example.planwithfriends.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long,
    val name: String,
    val email: String,
    val isCurrentUser: Boolean = false
)