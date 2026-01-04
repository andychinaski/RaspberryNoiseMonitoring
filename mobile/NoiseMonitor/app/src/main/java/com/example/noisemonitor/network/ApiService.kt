package com.example.noisemonitor.network

import com.example.noisemonitor.AlertEvent
import com.example.noisemonitor.DeviceInfo
import com.example.noisemonitor.HistoryEvent
import retrofit2.http.GET

interface ApiService {

    @GET("api/notifications")
    suspend fun getNotifications(): List<AlertEvent>

    @GET("api/device-info")
    suspend fun getDeviceInfo(): DeviceInfo

    @GET("/api/events")
    suspend fun getEvents(): List<HistoryEvent>

}
