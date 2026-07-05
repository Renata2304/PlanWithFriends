package com.example.planwithfriends.data.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").filter { it.isNotBlank() }
    }
}