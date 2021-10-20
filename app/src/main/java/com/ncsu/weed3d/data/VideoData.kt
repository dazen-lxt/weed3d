package com.ncsu.weed3d.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class VideoData(
    var farmName: String,
    var farmType: String,
    var remoteUrl: String,
    var weatherType: String,
    var height: Int,
    var date: String,
    var comments: String,
    var goProUri: String,
    var deviceUri: String,
    var thumbnailUri: String
) {
    @PrimaryKey(autoGenerate = true)
    var videoId: Int = 0
}