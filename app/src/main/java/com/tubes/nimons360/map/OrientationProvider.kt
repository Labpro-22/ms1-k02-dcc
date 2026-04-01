package com.tubes.nimons360.map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class OrientationProvider(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private val _azimuth = MutableLiveData<Float>(0f)
    val azimuth: LiveData<Float> = _azimuth

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity.copyFrom(event.values)
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic.copyFrom(event.values)
        }
        val r = FloatArray(9)
        val i = FloatArray(9)

        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            _azimuth.postValue(Math.toDegrees(orientation[0].toDouble()).toFloat())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

private fun FloatArray.copyFrom(src: FloatArray) = src.copyInto(this)