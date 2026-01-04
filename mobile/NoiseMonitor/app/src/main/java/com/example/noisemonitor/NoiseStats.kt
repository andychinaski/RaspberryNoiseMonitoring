package com.example.noisemonitor

import com.google.gson.annotations.SerializedName

data class NoiseStats(
    @SerializedName("current_noise")
    val currentNoise: Int,
    @SerializedName("current_timestamp")
    val currentTimestamp: String,
    val date: String,
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("last_10_minutes")
    val last10minutes: List<NoiseEvent>,
    @SerializedName("max_noise")
    val maxNoise: Int,
    @SerializedName("min_noise")
    val minNoise: Int,
    @SerializedName("notifications_sent")
    val notificationsSent: Int
)

data class NoiseEvent(
    @SerializedName("noise_level")
    val noiseLevel: Int,
    val timestamp: String
)
