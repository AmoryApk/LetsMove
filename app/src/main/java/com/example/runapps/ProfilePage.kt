package com.example.runapps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.MapsActivity
import com.example.runapps.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfilePage : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        // Initialize Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Set up Bottom Navigation item selected listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Navigate to Home Activity
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_activity -> {
                    // Navigate to Activity Page
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    // Already on Profile Page, do nothing
                    true
                }
                else -> false
            }
        }
    }
}