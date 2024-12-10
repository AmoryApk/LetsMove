package com.example.runapps.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.R
import com.example.runapps.authentication.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {
    private lateinit var editProfileName: EditText
    private lateinit var saveNameButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initializeViews()
        loadExistingProfileName()
        setupSaveNameButton()
        setupLogoutButton()
    }

    private fun initializeViews() {
        editProfileName = findViewById(R.id.editProfileName)
        saveNameButton = findViewById(R.id.saveNameButton)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun loadExistingProfileName() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val currentName = sharedPreferences.getString("profileName", null)
            ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
            ?: "User"
        editProfileName.setText(currentName)
    }

    private fun setupSaveNameButton() {
        saveNameButton.setOnClickListener {
            val newName = editProfileName.text.toString().trim()
            if (newName.isNotEmpty()) {
                saveProfileName(newName)
            } else {
                editProfileName.error = "Name cannot be empty"
            }
        }
    }

    private fun saveProfileName(newName: String) {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profileName", newName)
        editor.apply()
        saveUsernameToDatabase(newName)
    }

    private fun saveUsernameToDatabase(username: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users").child(uid!!)
        databaseReference.child("username").setValue(username)
            .addOnSuccessListener {
                showProfileNameUpdatedMessage()
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("SettingsActivity", "Error saving username to database", exception)
                showProfileNameUpdateFailedMessage()
            }
    }

    private fun showProfileNameUpdatedMessage() {
        Toast.makeText(this, "Profile name updated", Toast.LENGTH_SHORT).show()
    }

    private fun showProfileNameUpdateFailedMessage() {
        Toast.makeText(this, "Failed to update profile name", Toast.LENGTH_SHORT).show()
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        showLogoutMessage()
        redirectToIntroPage()
    }

    private fun showLogoutMessage() {
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun redirectToIntroPage() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
