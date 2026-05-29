package com.example.planwithfriends.data.network

import com.google.gson.annotations.SerializedName

data class NetworkGroupEvent(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("groupId") val groupId: String,
    @SerializedName("title") val title: String,
    @SerializedName("time") val time: String,
    @SerializedName("date") val date: String
)