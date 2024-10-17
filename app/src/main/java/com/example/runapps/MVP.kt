package com.example.runapps

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

class MapPresenter(private val activity: AppCompatActivity) {
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
            ui.value = current?.copy(formattedDistance = formattedDistance)
        }

        stepCounter.liveSteps.observe(activity) { steps ->
            val current = ui.value
            ui.value = current?.copy(formattedPace = "$steps")
        }
    }

    fun onMapLoaded() {
        permissionsManager.requestUserLocation()
    }

    fun startTracking() {
        permissionsManager.requestActivityRecognition()
        locationProvider.trackUser()

        val currentUi = ui.value
        ui.value = currentUi?.copy(
            formattedPace = Ui.EMPTY.formattedPace,
            formattedDistance = Ui.EMPTY.formattedDistance
        )
    }

    fun stopTracking() {
        locationProvider.stopTracking()
        stepCounter.unloadStepCounter()
    }
}

data class Ui(
    val formattedPace: String,
    val formattedDistance: String,
    val currentLocation: LatLng?,
    val userPath: List<LatLng>
) {

    companion object {

        val EMPTY = Ui(
            formattedPace = "",
            formattedDistance = "",
            currentLocation = null,
            userPath = emptyList()
        )
    }
}