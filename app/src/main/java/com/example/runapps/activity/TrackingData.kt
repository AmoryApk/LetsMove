package com.example.runapps.activity

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate

data class TrackingData(
//    @TypeConverters(LatLngListConverter::class)
//    val userPath: List<LatLng>,
//    val currentLocation: LatLng? = null,
    val distance: Int = 0,
    val pace: Int = 0,
    val step: Int = 0,
    val runningTime: Long = 0,
    val runningDate: String
) {
    // Add a no-argument constructor
    constructor() : this( 0, 0, 0, 0, "")
}