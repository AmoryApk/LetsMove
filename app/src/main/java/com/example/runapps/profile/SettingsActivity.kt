package com.example.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.R
import com.example.runapps.authentication.LoginActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var editProfileName: EditText
    private lateinit var saveNameButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        editProfileName = findViewById(R.id.editProfileName)
        saveNameButton = findViewById(R.id.saveNameButton)
        logoutButton = findViewById(R.id.logoutButton)

        // Load existing profile name
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val currentName = sharedPreferences.getString("profileName", "User")
        editProfileName.setText(currentName)

        // Save the new name with validation
        saveNameButton.setOnClickListener {
            val newName = editProfileName.text.toString().trim()
            if (newName.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("profileName", newName)
                editor.apply()

                // Show confirmation message
                Toast.makeText(this, "Profile name updated", Toast.LENGTH_SHORT).show()

                finish() // Close the settings page and go back
            } else {
                editProfileName.error = "Name cannot be empty"
            }
        }

        // Handle logout button click
        logoutButton.setOnClickListener {
            // Clear shared preferences or user session
            val editor = sharedPreferences.edit()
            editor.clear() // Clear all saved data
            editor.apply()

            // Show logout message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Redirect to login page
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
