package com.example.runapps.activity

import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.runapps.R
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.util.Date

class MapPresenter(private val activity: AppCompatActivity) {
//    var startTime = SystemClock.elapsedRealtime().toInt() / 1000
    private var startTime = 0L


    val ui = MutableLiveData(Ui.EMPTY)

    private val locationProvider = LocationProvider(activity)

    private val stepCounter = StepCounter(activity)

    private val permissionsManager = PermissionManager(activity, locationProvider, stepCounter)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()


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
        // Get the current user's UID
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val uiData = ui.value // Get the current UI state

            // Create a TrackingData object with raw values
            val trackingData = TrackingData(
//                userPath = uiData?.userPath?.map { LatLng(it.latitude, it.longitude) } ?: emptyList(),
//                currentLocation = uiData?.currentLocation,
                distance = locationProvider.distance, // Get raw distance from LocationProvider
                step = uiData?.formattedPace?.toIntOrNull() ?: 0,
                runningTime = ((SystemClock.elapsedRealtime() - startTime) / 1000),
                runningDate = LocalDate.now().toString()
            )
            // Create a reference to the user's tracking list
            val userTrackingListRef = database.reference.child("user_tracking").child(userId).child("trackingData")
            // Push a new entry to the list
            userTrackingListRef.push().setValue(trackingData)
                .addOnSuccessListener {
                    // Handle success
                    Log.d("MapPresenter", "Tracking data added to list successfully")
                    Toast.makeText(activity, "Tracking data saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Log.e("MapPresenter", "Error adding tracking data to list: ${exception.message}", exception)
                    Toast.makeText(activity, "Error saving tracking data", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User is not logged in, handle accordingly (e.g., show a login prompt)
            Log.w("MapPresenter", "User not logged in, cannot save tracking data")
            Toast.makeText(activity, "Please log in to save tracking data", Toast.LENGTH_SHORT).show()
        }
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
