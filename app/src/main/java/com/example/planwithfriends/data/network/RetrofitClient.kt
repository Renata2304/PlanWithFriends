package com.example.planwithfriends.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://6a15dbd091ff9a63de08e8a1.mockapi.io/"

    val apiService: GroupApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroupApiService::class.java)
    }
}