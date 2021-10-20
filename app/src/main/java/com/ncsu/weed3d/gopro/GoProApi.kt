package com.ncsu.weed3d.gopro

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoProApi {

    @GET("gp/gpControl/command/system/locate")
    fun locate(@Query("p") p: Int): Call<Void>


    @GET("gp/gpControl/command/shutter")
    fun triggerShutter(@Query("p") p: Int): Call<Void>

}