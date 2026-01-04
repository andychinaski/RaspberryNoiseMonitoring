package com.example.noisemonitor

import com.google.gson.annotations.SerializedName

data class HistoryEvent(
    val id: Int,
    val info: String?,
    @SerializedName("noise_level")
    val noiseLevel: Int,
    val timestamp: String,
    val type: String
)
