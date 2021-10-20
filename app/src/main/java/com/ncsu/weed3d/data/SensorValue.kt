package com.ncsu.weed3d.data

import android.hardware.Sensor

class SensorValue(val sensor: Sensor, val isActive: Boolean) {
    var values: MutableList<SensorValueTime> = mutableListOf()
    var unit = when(sensor.type) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "m/s2"
        Sensor.TYPE_GYROSCOPE -> "rad/s"
        Sensor.TYPE_LIGHT -> "lux"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
        Sensor.TYPE_PRESSURE -> "hPa"
        Sensor.TYPE_PROXIMITY -> "cm"
        else -> ""
    }
    var type = when(sensor.type) {
        Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "Accelerometer Uncalibrated"
        Sensor.TYPE_GYROSCOPE -> "Gyroscope"
        Sensor.TYPE_LIGHT -> "Light"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature"
        Sensor.TYPE_PRESSURE -> "Pressure"
        Sensor.TYPE_PROXIMITY -> "Proximity"
        else -> ""
    }

}

data class SensorValueTime(val time: Long, val values: List<Float>) {
    var dataBaseId: Long = 0
}