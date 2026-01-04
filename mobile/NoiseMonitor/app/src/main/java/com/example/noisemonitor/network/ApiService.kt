package com.example.noisemonitor.network

import com.example.noisemonitor.AlertEvent
import retrofit2.http.GET

interface ApiService {

    @GET("api/notifications")
    suspend fun getNotifications(): List<AlertEvent>
}
