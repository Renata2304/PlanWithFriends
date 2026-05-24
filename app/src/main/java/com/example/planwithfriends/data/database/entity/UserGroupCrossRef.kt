package com.example.planwithfriends.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_group_cross_ref",
    primaryKeys = ["userId", "groupId"]
)
data class UserGroupCrossRef(
    val userId: String,
    val groupId: String
)