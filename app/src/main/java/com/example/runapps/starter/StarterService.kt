package com.example.runapps.starter

import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StarterService {
    fun calculateCalories(distance: Double, pace: Double): Double {
        val calories = (distance / 1000) * 0.75
        Log.i("Calories", (calories.toString()))
        return calories
    }

    fun calculatePace(distance: Double, time: Double): Double {
        val pace = distance / time
        Log.i("Pace", (pace.toString()))
        return pace
    }

    fun convertMeterToKm(distance: Double): Double {
        val km = distance / 1000
        Log.i("Km", (km.toString()))
        return km
    }

    fun formatDateToReadable(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        val date = LocalDate.parse(dateString, inputFormatter)
        return date.format(outputFormatter)
    }

    fun convertSecondToHour(second: Double): Double {
        val hour = second / 3600
        Log.i("Hour", (hour.toString()))
        return hour
    }
}