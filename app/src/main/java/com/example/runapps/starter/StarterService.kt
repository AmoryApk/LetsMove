package com.example.runapps.starter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
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

    fun cropToCircle(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        return output
    }

    fun loadProfileImage(imageView: ImageView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("profile_images").child(uid)
            storageReference.getBytes(1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                val circularBitmap = cropToCircle(bitmap)
                imageView.setImageBitmap(circularBitmap)
            }.addOnFailureListener {
                Log.e("ProfileImage", "Error loading image", it)
            }
        }
    }

    fun loadProfileName(profileName: TextView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users").child(uid!!)

        databaseReference.child("username").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val username = dataSnapshot.getValue(String::class.java)
                if (username != null) {
                    // Username found in database, use it
                    profileName.text = username
                } else {
                    // Username not set, extract from email
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    val extractedUsername = email?.substringBefore("@")
                    profileName.text = extractedUsername ?: "User"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProfilePage", "Error loading profile name", databaseError.toException())
                profileName.text = "User" // Fallback to default value
            }
        })
    }
}