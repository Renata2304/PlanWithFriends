package com.example.planwithfriends.data.network

import com.google.gson.annotations.SerializedName

data class NetworkGroup(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("groupId") val groupId: String,
    @SerializedName("name") val name: String,
    @SerializedName("memberCount") val memberCount: Int,
    @SerializedName("memberIcons") val memberIcons: List<String> = emptyList(),
    @SerializedName("members") val members: List<String> = emptyList()
)