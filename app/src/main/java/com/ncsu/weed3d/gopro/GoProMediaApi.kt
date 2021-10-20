package com.ncsu.weed3d.gopro

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

import okhttp3.ResponseBody

import retrofit2.http.Streaming




interface GoProMediaApi {

    @GET("gp/gpMediaList")
    fun getMedia(): Call<MediaResponse>

    @Streaming
    @GET
    fun downloadFileByUrl(@Url fileUrl: String): Call<ResponseBody>
}