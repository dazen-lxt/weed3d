package com.ncsu.weed3d.gopro

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import com.google.gson.GsonBuilder


object RetrofitInstance {

    private var retrofitInstance: GoProApi? = null
    private var retrofitMediaInstance: GoProMediaApi? = null

    fun getInstance(): GoProApi {
        return retrofitInstance ?: run {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://10.5.5.9/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            retrofitInstance = retrofit.create(GoProApi::class.java)
            retrofitInstance!!
        }
    }

    fun getMediaInstance(): GoProMediaApi {
        return retrofitMediaInstance ?: run {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://10.5.5.9:8080/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            retrofitMediaInstance = retrofit.create(GoProMediaApi::class.java)
            retrofitMediaInstance!!
        }
    }
}