package com.ncsu.weed3d.gopro

import com.google.gson.annotations.SerializedName

data class MediaResponse (
        @SerializedName("id")
        var id: String,
        @SerializedName("media")
        var media: List<MediaResponseGoPro>,
)

data class MediaResponseGoPro (
        @SerializedName("d")
        var d: String,
        @SerializedName("fs")
        var fs: List<MediaResponseFile>,
)

data class MediaResponseFile (
        @SerializedName("n")
        var n: String,
        @SerializedName("cre")
        var cre: String,
        @SerializedName("mod")
        var mod: String,
        @SerializedName("ls")
        var ls: String,
        @SerializedName("s")
        var s: String,
)