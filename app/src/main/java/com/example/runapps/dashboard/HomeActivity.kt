package com.example.runapps.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runapps.activity.MapsActivity
import com.example.runapps.profile.ProfilePage
import com.example.runapps.R
import com.example.runapps.activity.RecentActivity
import com.example.runapps.activity.TrackingData
import com.example.runapps.authentication.RecentActivityAdapter
import com.example.runapps.starter.StarterService
import com.example.runapps.starter.StarterService.calculateCalories
import com.example.runapps.starter.StarterService.calculatePace
import com.example.runapps.starter.StarterService.convertMeterToKm
import com.example.runapps.starter.StarterService.convertSecondToHour
import com.example.runapps.starter.StarterService.formatDateToReadable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.getValue
import kotlin.text.format

class HomeActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var recentActivityRecyclerView: RecyclerView
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var recentActivityList: ArrayList<RecentActivity>
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var welcomeTextView: TextView // Declare the TextView for welcome message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView) // Initialize the TextView

        // Initialize RecyclerView
        recentActivityRecyclerView = findViewById(R.id.recentActivityRecyclerView)
        recentActivityRecyclerView.layoutManager = LinearLayoutManager(this)
        recentActivityRecyclerView.setHasFixedSize(true)

        // Initialize Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Load initial sample data for recent activities
//        loadSampleData()
        fetchRecentActivityData { recentActivityList ->
            // Use the recentActivityList here
            recentActivityAdapter = RecentActivityAdapter(recentActivityList)
            recentActivityRecyclerView.adapter = recentActivityAdapter
        }

        // Initialize Adapter and set it to RecyclerView
//        recentActivityAdapter = RecentActivityAdapter(recentActivityList)
//        recentActivityRecyclerView.adapter = recentActivityAdapter

        // Set welcome message with user name
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val email = currentUser.email
            val userName = email?.substringBefore("@") // Extracting name from email
            welcomeTextView.text = "Hello, $userName" // Set the welcome message
        }

        // Set up Bottom Navigation item selected listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle Home navigation
                    true
                }
                R.id.navigation_activity -> {
                    // Handle Activity navigation
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    // Handle Profile navigation
                    startActivity(Intent(this, ProfilePage::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchRecentActivityData(callback: (ArrayList<RecentActivity>) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val recentActivityRef = database.reference.child("user_tracking").child(userId).child("trackingData")

            recentActivityRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val recentActivityList = ArrayList<RecentActivity>() // Initialize here

                    for (snapshot in dataSnapshot.children) {
                        val trackingData = snapshot.getValue(TrackingData::class.java)
                        val distance = convertMeterToKm(trackingData?.distance?.toDouble() ?: 0.0)
                        val calories = calculateCalories(trackingData?.distance?.toDouble() ?: 0.0, trackingData?.pace?.toDouble() ?: 0.0)
                        val pace = calculatePace(distance, convertSecondToHour(trackingData?.runningTime?.toDouble() ?: 0.0))

                        if (trackingData != null) {
                            val recentActivity = RecentActivity(
                                formatDateToReadable(trackingData.runningDate),
                                String.format("%.3f", distance) + " Km",
                                String.format("%.3f", calories),
                                String.format("%.3f", pace)
                            )
                            recentActivityList.add(recentActivity)
                        }
                    }

                    callback(recentActivityList) // Invoke the callback with the list
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Log.e("HomeActivity", "Error fetching recent activity data: ${databaseError.message}", databaseError.toException())
                    callback(ArrayList()) // Invoke callback with empty list in case of error
                }
            })
        } else {
            // User is not logged in, handle accordingly
            callback(ArrayList()) // Invoke callback with empty list if user is not logged in
        }
    }

    // Method to load sample data
//    private fun loadSampleData() {
//        recentActivityList = arrayListOf(
//            RecentActivity("September 19", "10,12 km", "701", "11,2"),
//            RecentActivity("September 18", "9,89 km", "669", "10,8"),
//            RecentActivity("September 16", "9,12 km", "608", "10,1")
//        )
//    }



}