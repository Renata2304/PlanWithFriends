package com.example.planwithfriends.data

data class Group(
    val id: String,
    val name: String,
    val memberCount: Int,
    val memberIcons: List<String>
)