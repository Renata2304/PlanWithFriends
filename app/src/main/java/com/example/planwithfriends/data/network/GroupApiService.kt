package com.example.planwithfriends.data.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupApiService {
    @GET("users")
    suspend fun getUserByUsername(@Query("q") query: String): List<NetworkUser>

    @POST("users")
    suspend fun createUser(@Body user: NetworkUser): NetworkUser

    @GET("groups")
    suspend fun getAllGroups(): List<NetworkGroup>

    @GET("groups")
    suspend fun getGroupByCode(@Query("q") query: String): List<NetworkGroup>

    @POST("groups")
    suspend fun createGroup(@Body group: NetworkGroup): NetworkGroup

    @PUT("groups/{id}")
    suspend fun updateGroup(@Path("id") objectId: String, @Body group: NetworkGroup)

    @DELETE("groups/{id}")
    suspend fun deleteGroup(@Path("id") objectId: String)

    @GET("events")
    suspend fun getEventsForGroup(@Query("q") query: String): List<NetworkGroupEvent>

    @POST("events")
    suspend fun createGroupEvent(@Body event: NetworkGroupEvent): NetworkGroupEvent

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") objectId: String, @Body event: NetworkGroupEvent)

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") objectId: String)
}