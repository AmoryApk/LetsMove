package com.example.runapps.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.R

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        // Optionally, customize the back button behavior
        // No additional code needed if you just want the default behavior
    }

    // Override onBackPressed to go back to ProfilePage
    override fun onBackPressed() {
        super.onBackPressed()
        // Optionally, add custom transition or action
    }
}
