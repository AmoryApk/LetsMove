package com.example.runapps.activity

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
class LocationProvider(private val activity: AppCompatActivity) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(activity) }
    private val locations = mutableListOf<LatLng>()
    var distance = 0
    val liveLocations = MutableLiveData<List<LatLng>>()
    val liveDistance = MutableLiveData<Int>()
    val livePace = MutableLiveData<Double>()
    val liveLocation = MutableLiveData<LatLng>()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            Log.d("LocationProvider", "onLocationResult called")
            val currentLocation = result.lastLocation
            val latLng = currentLocation?.let { LatLng(currentLocation.latitude, it.longitude) }
            if (latLng == null) {
                // If latLng is null, request location again
                client.lastLocation.addOnSuccessListener { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    if (newLatLng != null) {
                        locations.add(newLatLng)
                        liveLocation.value = newLatLng
                    }
                }
            } else {
                val lastLocation = locations.lastOrNull()
                if (lastLocation != null) {
                    distance += SphericalUtil.computeDistanceBetween(lastLocation, latLng).roundToInt()
                    liveDistance.value = distance
                }
                locations.add(latLng)
                liveLocations.value = locations
            }
        }
    }
    fun getUserLocation() {
        Log.d("LocationProvider", "getUserLocation called")
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setWaitForAccurateLocation(true) // Wait for a more accurate location
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    locations.add(latLng)
                    liveLocation.value = latLng
                    client.removeLocationUpdates(this) // Remove updates after receiving location
                }
            }
        }

        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    fun trackUser() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000) // Optional: Set minimum update interval
            .build()
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopTracking() {
        client.removeLocationUpdates(locationCallback)
        locations.clear()
        distance = 0
    }
}

