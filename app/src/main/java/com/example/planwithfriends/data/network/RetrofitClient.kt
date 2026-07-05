package com.example.planwithfriends.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://planwithfriends-6e19.restdb.io/rest/"
    private const val API_KEY = "32e676d3b8db54dbece04f52487d4e7b90817"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }.build()

    val apiService: GroupApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroupApiService::class.java)
    }
}