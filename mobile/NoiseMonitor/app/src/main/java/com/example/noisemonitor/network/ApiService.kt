package com.example.noisemonitor.network

import com.example.noisemonitor.AlertEvent
import com.example.noisemonitor.DeviceInfo
import com.example.noisemonitor.HistoryEvent
import com.example.noisemonitor.NoiseStats
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("date") date: String,
        @Query("only_sent") onlySent: Int
    ): List<AlertEvent>

    @GET("api/device-info")
    suspend fun getDeviceInfo(): DeviceInfo

    @GET("/api/events")
    suspend fun getEvents(
        @Query("date") date: String,
        @Query("only_critical") onlyCritical: Int
    ): List<HistoryEvent>

    @GET("/api/noise-stats")
    suspend fun getNoiseStats(@Query("date") date: String): NoiseStats

}
