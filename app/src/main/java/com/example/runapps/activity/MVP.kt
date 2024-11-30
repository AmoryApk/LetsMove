package com.example.runapps

import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import kotlin.time.Duration.Companion.seconds

class MapPresenter(private val activity: AppCompatActivity) {
//    var startTime = SystemClock.elapsedRealtime().toInt() / 1000
    private var startTime = 0L


    val ui = MutableLiveData(Ui.EMPTY)

    private val locationProvider = LocationProvider(activity)

    private val stepCounter = StepCounter(activity)

    private val permissionsManager = PermissionManager(activity, locationProvider, stepCounter)

    fun onViewCreated() {

        locationProvider.liveLocations.observe(activity) { locations ->
            val current = ui.value
            ui.value = current?.copy(userPath = locations)
        }

        locationProvider.liveLocation.observe(activity) { currentLocation ->
            val current = ui.value
            ui.value = current?.copy(currentLocation = currentLocation)
        }

        locationProvider.liveDistance.observe(activity) { distance ->
            val current = ui.value
            val formattedDistance = activity.getString(R.string.distance_value, distance)
            Log.d("Location", "formattedDistance: $formattedDistance")
            ui.value = current?.copy(formattedDistance = formattedDistance)
        }

        locationProvider.liveDistance.observe(activity) { distance ->
            val elapsedTimeInSeconds = ((SystemClock.elapsedRealtime() - startTime) / 1000).toInt()
            Log.d("Location", "elapsedTimeInSeconds: $elapsedTimeInSeconds")
            Log.d("Location", "distance: $distance")
            Log.d("Location", "startTime: $startTime")
            Log.d("Location", "elapsedTimeInSeconds: $elapsedTimeInSeconds")
            val metersPerSecond = if (elapsedTimeInSeconds > 0) distance / elapsedTimeInSeconds else 0
            Log.d("Location", "metersPerSecond: $metersPerSecond")
            val formattedDistancePerTime = activity.getString(R.string.distance_per_time_value, metersPerSecond)
            Log.d("Location", "formattedDistancePerTime: $formattedDistancePerTime")
            val current = ui.value
            ui.value = current?.copy(formattedDistancePerTime = formattedDistancePerTime)
        }

        stepCounter.liveSteps.observe(activity) { steps ->
            Log.d("MapPresenter", "Steps: $steps")
            val current = ui.value
            ui.value = current?.copy(formattedPace = "$steps")
        }
    }

    fun onMapLoaded() {
        permissionsManager.requestUserLocation()
    }

    fun startTracking(): Boolean {
        Log.i("MapPresenter", "startTracking called")
        permissionsManager.requestActivityRecognition()
        if (permissionsManager.isGpsEnabled(activity)) {
            locationProvider.trackUser()
            startTime = SystemClock.elapsedRealtime()
            val currentUi = ui.value
            ui.value = currentUi?.copy(
                formattedPace = Ui.EMPTY.formattedPace,
                formattedDistance = Ui.EMPTY.formattedDistance
            )
            return true
        } else {
            Log.d("MapPresenter", "GPS disabled, showing dialog")
            permissionsManager.showGpsDisabledDialog(activity)
            return false
        }
    }

    fun stopTracking() {
        locationProvider.stopTracking()
        stepCounter.unloadStepCounter()
    }
}

data class Ui(
    val formattedPace: String,
    val formattedDistance: String,
    val formattedDistancePerTime: String,
    val currentLocation: LatLng?,
    val userPath: List<LatLng>
) {

    companion object {

        val EMPTY = Ui(
            formattedPace = "",
            formattedDistance = "",
            formattedDistancePerTime = "",
            currentLocation = null,
            userPath = emptyList()
        )
    }
}