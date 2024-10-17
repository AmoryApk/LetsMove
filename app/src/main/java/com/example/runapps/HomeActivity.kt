package com.example.runapps

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runapps.MapsActivity
import com.example.runapps.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

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
        loadSampleData()

        // Initialize Adapter and set it to RecyclerView
        recentActivityAdapter = RecentActivityAdapter(recentActivityList)
        recentActivityRecyclerView.adapter = recentActivityAdapter

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

    // Method to load sample data
    private fun loadSampleData() {
        recentActivityList = arrayListOf(
            RecentActivity("September 19", "10,12 km", "701", "11,2"),
            RecentActivity("September 18", "9,89 km", "669", "10,8"),
            RecentActivity("September 16", "9,12 km", "608", "10,1")
        )
    }
}