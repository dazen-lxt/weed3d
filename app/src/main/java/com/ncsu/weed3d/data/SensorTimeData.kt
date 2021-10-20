package com.ncsu.weed3d.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SensorTimeData(
    var videoId: Long,
    var sensorName: String,
    var time: Long,
    var value: String
) {
    @PrimaryKey(autoGenerate = true)
    var sensorTimeId: Int = 0
}