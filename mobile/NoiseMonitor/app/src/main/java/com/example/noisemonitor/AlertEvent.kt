package com.example.noisemonitor

import com.google.gson.annotations.SerializedName

data class AlertEvent(
    val id: Long,
    val message: String,

    @SerializedName("sent_at")
    val sentAt: String,
    val status: String
)
