package com.example.noisemonitor

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("device_name")
    val deviceName: String,

    @SerializedName("measurement_frequency")
    val measurementFrequency: Int,

    val uptime: Int
)

data class DeviceReboot(
    val message: String,
    val status: String
)