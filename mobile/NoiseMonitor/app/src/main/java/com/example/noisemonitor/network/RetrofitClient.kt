package com.example.noisemonitor.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var retrofit: Retrofit? = null

    fun getApi(context: Context): ApiService {
        if (retrofit == null) {
            buildRetrofit(context)
        }
        return retrofit!!.create(ApiService::class.java)
    }

    fun rebuild(context: Context) {
        buildRetrofit(context)
    }

    private fun buildRetrofit(context: Context) {
        val ip = NetworkConfig.getServerIp(context)

        retrofit = Retrofit.Builder()
            .baseUrl("http://$ip/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
