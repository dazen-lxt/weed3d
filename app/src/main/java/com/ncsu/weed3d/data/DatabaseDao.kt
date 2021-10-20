package com.ncsu.weed3d.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DatabaseDao {

    @Query("SELECT * FROM VideoData")
    fun getAllVideo(): List<VideoData>

    @Query("SELECT * FROM VideoData WHERE farmName = :farmName")
    fun getVideoOfFarm(farmName: String): List<VideoData>

    @Query("SELECT * FROM VideoData WHERE farmName = :farmName AND farmType = :farmType AND date = :farmDate")
    fun getVideoOfType(farmName: String, farmType: String, farmDate: String): List<VideoData>

    @Query("SELECT * FROM SensorTimeData WHERE videoId = :videoId")
    fun getSensorDataByVideoId(videoId: Int): List<SensorTimeData>

    @Insert
    fun insertVideo(videoData: VideoData): Long

    @Insert
    fun insertSensorTime(sensorTimeData: List<SensorTimeData>)

    @Update
    fun updateVideo(videoData: VideoData): Int

    @Query("SELECT * FROM FarmData")
    fun getAllFarms(): List<FarmData>

    @Insert
    fun insertFarm(farmData: FarmData)
}