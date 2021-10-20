package com.ncsu.weed3d.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FarmData(
    var farmName: String
) {
    @PrimaryKey(autoGenerate = true)
    var farmId: Int = 0
}