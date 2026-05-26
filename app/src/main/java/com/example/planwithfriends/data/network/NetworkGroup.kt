package com.example.planwithfriends.data.network

import com.google.gson.annotations.SerializedName

data class NetworkGroup(
    @SerializedName("groupId") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("memberCount") val memberCount: Int
)