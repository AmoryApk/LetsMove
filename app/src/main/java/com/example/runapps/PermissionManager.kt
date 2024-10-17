package com.example.runapps

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PermissionManager(activity: AppCompatActivity,
                        private val locationProvider: LocationProvider,
                        private val stepCounter: StepCounter)  {


    private val locationPermissionProvider = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d("PermissionManager", "Location permission granted")
            if (isGpsEnabled(activity)) {
                Log.d("PermissionManager", "GPS enabled, getting user location")
                locationProvider.getUserLocation()
            } else {
                // GPS is disabled, show a dialog or message
                showGpsDisabledDialog(activity)
                Log.d("PermissionManager", "GPS disabled, showing dialog")
            }
        }
    }

    private fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showGpsDisabledDialog(activity: Activity) {
        // Create and show a dialog to prompt the user to enable GPS
        // You can use AlertDialog.Builder or a custom dialog
        AlertDialog.Builder(activity)
            .setTitle("GPS Disabled")
            .setMessage("Please enable GPS to use location features.")
            .setPositiveButton("Enable") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private val activityRecognitionPermissionProvider = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            stepCounter.setupStepCounter()
        }
    }

    fun requestUserLocation() {
        locationPermissionProvider.launch(ACCESS_FINE_LOCATION)
    }

    fun requestActivityRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionPermissionProvider.launch(ACTIVITY_RECOGNITION)
        } else {
            stepCounter.setupStepCounter()
        }
    }
}