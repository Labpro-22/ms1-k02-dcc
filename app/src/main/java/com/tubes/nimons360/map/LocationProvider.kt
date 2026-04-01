package com.tubes.nimons360.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationProvider(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            _location.postValue(result.lastLocation)
        }
    }

    @SuppressLint("MissingPermission")
    fun start() = fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

    fun stop() = fusedClient.removeLocationUpdates(callback)
}