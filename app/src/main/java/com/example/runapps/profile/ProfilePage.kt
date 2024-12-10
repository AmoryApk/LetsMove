package com.example.runapps.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.BitmapShader
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.runapps.R
import com.example.runapps.activity.MapsActivity
import com.example.runapps.dashboard.HomeActivity
import com.example.runapps.activity.TrackingData
import com.example.runapps.starter.StarterService.calculateCalories
import com.example.runapps.starter.StarterService.convertMeterToKm
import com.example.runapps.starter.StarterService.convertSecondToHour
import com.example.runapps.starter.StarterService.cropToCircle
import com.example.runapps.starter.StarterService.loadProfileImage
import com.example.runapps.starter.StarterService.loadProfileName
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import kotlin.text.format

class ProfilePage : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var timeText: TextView
    private lateinit var distanceText: TextView
    private lateinit var caloriesText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)
        initializeViews()
        fetchRecentActivityData()
        loadProfileName(profileName)
        loadProfileImage(profileImage)
        setupProfileImageClickListener()
        setupBottomNavigation()
        setupMenuClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadProfileName(profileName)
    }

    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        profileName = findViewById(R.id.profileName)
        profileImage = findViewById(R.id.profileImage)
        timeText = findViewById(R.id.timeText)
        distanceText = findViewById(R.id.distanceText)
        caloriesText = findViewById(R.id.caloriesText)
    }

    private fun fetchRecentActivityData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val recentActivityRef = database.reference.child("user_tracking").child(userId).child("trackingData")
            recentActivityRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val totals = calculateTotals(dataSnapshot)
                    updateUI(totals)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    handleFetchError(databaseError)
                }
            })
        } else {
            handleUserNotLoggedIn()
        }
    }

//    private fun loadProfileName() {
//        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
//        val currentName = sharedPreferences.getString("profileName", null)
//            ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
//            ?: "User"
//        profileName.text = currentName
//    }



    private fun setupProfileImageClickListener() {
        profileImage.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                saveProfileImage(imageUri)
                loadProfileImage(profileImage)
            }
        }
    }

    private fun saveProfileImage(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("profile_images").child(uid)
            // Use Coroutines for asynchronous operations
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    val circularBitmap = cropToCircle(bitmap)
                    val baos = ByteArrayOutputStream()
                    circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val data = baos.toByteArray()
                    val uploadTask = storageReference.putBytes(data)
                    uploadTask.await() // Wait for upload to complete
                    withContext(Dispatchers.Main) {
                        Log.d("ProfileImage", "Image uploaded successfully")
                        loadProfileImage(profileImage)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("ProfileImage", "Error uploading image", e)
                        // Handle the error, e.g., show a toast message
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToActivity(HomeActivity::class.java)
                    true
                }
                R.id.navigation_activity -> {
                    navigateToActivity(MapsActivity::class.java)
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }

    private fun setupMenuClickListeners() {
        findViewById<View>(R.id.achievementsMenu).setOnClickListener {
            navigateToActivity(AchievementsActivity::class.java)
        }
        findViewById<View>(R.id.settingsMenu).setOnClickListener {
            navigateToActivity(SettingsActivity::class.java)
        }
        findViewById<View>(R.id.aboutUsMenu).setOnClickListener {
            navigateToActivity(AboutUsActivity::class.java)
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        startActivity(
            Intent(this, targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )
    }

    private fun calculateTotals(dataSnapshot: DataSnapshot): Totals {
        var totalTime = 0.0
        var totalDistance = 0.0
        var totalCalories = 0.0
        for (snapshot in dataSnapshot.children) {
            val trackingData = snapshot.getValue(TrackingData::class.java)
            if (trackingData != null) {
                val distance = convertMeterToKm(trackingData.distance.toDouble())
                val calories = calculateCalories(trackingData.distance.toDouble(), trackingData.pace.toDouble())
                val time = convertSecondToHour(trackingData.runningTime.toDouble())
                totalTime += time
                totalDistance += distance
                totalCalories += calories
            }
        }
        return Totals(totalTime, totalDistance, totalCalories)
    }

    private fun updateUI(totals: Totals) {
        val decimalFormat = DecimalFormat("0.##")
        timeText.text = decimalFormat.format(totals.time) + " Hour"
        distanceText.text = decimalFormat.format(totals.distance) + " Km"
        caloriesText.text = decimalFormat.format(totals.calories) + " Kcal"
    }

    private fun handleFetchError(databaseError: DatabaseError) {
        Log.e("HomeActivity", "Error fetching recent activity data: ${databaseError.message}", databaseError.toException())
    }

    private fun handleUserNotLoggedIn() {
        // User is not logged in, handle accordingly
    }

    data class Totals(val time: Double, val distance: Double, val calories: Double)
}
