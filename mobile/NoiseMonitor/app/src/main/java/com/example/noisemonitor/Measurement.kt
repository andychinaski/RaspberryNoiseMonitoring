package com.example.noisemonitor

data class Measurement(
    val time: String,
    val dbLevel: Int,
    val status: String
)