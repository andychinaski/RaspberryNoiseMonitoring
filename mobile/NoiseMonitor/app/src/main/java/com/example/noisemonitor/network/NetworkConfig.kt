package com.example.noisemonitor.network

import android.content.Context

object NetworkConfig {

    private const val PREFS_NAME = "network_prefs"
    private const val KEY_SERVER_IP = "server_ip"

    const val DEFAULT_IP = "192.168.0.1:5000"

    fun getServerIp(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_IP, DEFAULT_IP) ?: DEFAULT_IP
    }

    fun saveServerIp(context: Context, ip: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_IP, ip).apply()
    }
}
