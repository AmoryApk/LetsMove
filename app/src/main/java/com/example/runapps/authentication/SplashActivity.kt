package com.example.runapps.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.R
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private val splashScreenDuration = 1000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Ensure this layout exists in your project

        // Start a coroutine for the splash screen delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(splashScreenDuration)
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // Navigate based on login status
        if (isLoggedIn) {
            startActivity(Intent(this, com.example.runapps.dashboard.HomeActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Finish SplashActivity to remove it from the back stack
        finish()
    }
}