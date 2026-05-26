package com.example.planwithfriends.data.network

import com.google.gson.annotations.SerializedName

data class NetworkGroupEvent(
    @SerializedName("id") val id: String,
    @SerializedName("groupId") val groupId: String,
    @SerializedName("title") val title: String,
    @SerializedName("time") val time: String,
    @SerializedName("date") val date: String
)