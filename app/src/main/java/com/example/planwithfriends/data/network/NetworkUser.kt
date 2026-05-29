package com.example.planwithfriends.data.network

import com.google.gson.annotations.SerializedName

data class NetworkUser(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String = "$username@gmail.com",
    @SerializedName("active") val active: Boolean = true
)