package com.example.planwithfriends.data.network

import com.example.planwithfriends.data.network.NetworkGroup
import com.example.planwithfriends.data.network.NetworkGroupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GroupApiService {

    @GET("events")
    suspend fun getEventsForGroup(@Query("groupId") groupId: String): List<NetworkGroupEvent>

    @POST("events")
    suspend fun createGroupEvent(@Body event: NetworkGroupEvent): NetworkGroupEvent

    @GET("groups")
    suspend fun getAllGroups(): List<NetworkGroup>

    @POST("groups")
    suspend fun createGroup(@Body group: NetworkGroup): NetworkGroup

    @GET("groups")
    suspend fun getGroupByCode(@Query("groupId") groupId: String): List<NetworkGroup>
}