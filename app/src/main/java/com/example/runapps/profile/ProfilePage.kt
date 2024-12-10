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
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.R
import com.example.runapps.activity.MapsActivity
import com.example.runapps.dashboard.HomeActivity
import com.example.runapps.activity.TrackingData
import com.example.runapps.starter.StarterService.calculateCalories
import com.example.runapps.starter.StarterService.convertMeterToKm
import com.example.runapps.starter.StarterService.convertSecondToHour
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

        // Inisialisasi komponen UI
        bottomNavigation = findViewById(R.id.bottomNavigation)
        profileName = findViewById(R.id.profileName)
        profileImage = findViewById(R.id.profileImage)
        timeText = findViewById(R.id.timeText)
        distanceText = findViewById(R.id.distanceText)
        caloriesText = findViewById(R.id.caloriesText)

        fetchRecentActivityData()
        // Muat nama profil
        loadProfileName()

        // Muat gambar profil
        loadProfileImage()

        // Atur aksi klik pada gambar profil
        profileImage.setOnClickListener {
            openGallery()
        }

        // Setup Bottom Navigation dan menu
        setupBottomNavigation()
        setupMenuClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Perbarui nama profil jika berubah di Settings
        loadProfileName()
    }

    /**
     * Membuka galeri perangkat untuk memilih gambar
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    /**
     * Callback setelah pengguna memilih gambar dari galeri
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                saveProfileImage(imageUri)
                loadProfileImage() // Pastikan memuat ulang setelah menyimpan
            }
        }
    }

    /**
     * Memuat nama profil dari SharedPreferences
     */
    private fun loadProfileName() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("profileName", "User")
        profileName.text = name ?: "User"
    }

    /**
     * Memuat gambar profil dari file cache
     */
    private fun loadProfileImage() {
        val imageFile = File(cacheDir, "profile_image.png")
        if (imageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val circularBitmap = cropToCircle(bitmap)
            profileImage.setImageBitmap(circularBitmap)
        } else {
            profileImage.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    /**
     * Menyimpan gambar profil ke file cache
     */
    private fun saveProfileImage(imageUri: Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Konversi ke bentuk lingkaran
        val circularBitmap = cropToCircle(bitmap)

        // Simpan gambar lingkaran ke cache
        val imageFile = File(cacheDir, "profile_image.png")
        val outputStream = FileOutputStream(imageFile)
        circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    /**
     * Memotong bitmap menjadi lingkaran
     */
    private fun cropToCircle(bitmap: Bitmap): Bitmap {
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

    /**
     * Setup navigasi bawah
     */
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

    /**
     * Navigasi ke aktivitas lain
     */
    private fun navigateToActivity(targetActivity: Class<*>) {
        startActivity(
            Intent(this, targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )
    }

    private fun fetchRecentActivityData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val recentActivityRef = database.reference.child("user_tracking").child(userId).child("trackingData")
            recentActivityRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var totalTime = 0.0
                    var totalDistance = 0.0
                    var totalCalories = 0.0

                    for (snapshot in dataSnapshot.children) {
                        val trackingData = snapshot.getValue(TrackingData::class.java)
                        val distance = convertMeterToKm(trackingData?.distance?.toDouble() ?: 0.0)
                        val calories = calculateCalories(trackingData?.distance?.toDouble() ?: 0.0, trackingData?.pace?.toDouble() ?: 0.0)
                        val time = convertSecondToHour(trackingData?.runningTime?.toDouble() ?: 0.0)

                        if (trackingData != null) {
                            totalTime += time
                            totalDistance += distance
                            totalCalories += calories
                        }
                    }

                    timeText.text = String.format("%.3f", totalTime) + " Hour"
                    distanceText.text = String.format("%.3f", totalDistance) + " Km"
                    caloriesText.text = String.format("%.3f", totalCalories) + " Kcal"
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Log.e("HomeActivity", "Error fetching recent activity data: ${databaseError.message}", databaseError.toException())
                }
            })
        } else {
            // User is not logged in, handle accordingly
        }
    }
}
