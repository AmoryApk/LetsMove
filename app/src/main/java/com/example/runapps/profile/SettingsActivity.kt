package com.example.runapps.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.R
import com.example.runapps.authentication.MainActivity
import com.example.runapps.authentication.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.runapps.starter.StarterService.loadProfileImage

class SettingsActivity : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var profileName: TextView
    private lateinit var profileImage: ImageView // Declare the ImageView
    private lateinit var cancelButton: TextView
    private lateinit var saveButton: TextView
    private lateinit var emailField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initializeViews()
        loadExistingProfileName()
        setupListeners()
    }

    private fun initializeViews() {
        usernameField = findViewById(R.id.usernameField)
        logoutButton = findViewById(R.id.logoutButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        profileName = findViewById(R.id.profileName)
        profileImage = findViewById(R.id.profileImage)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
        emailField = findViewById(R.id.emailField)

        loadProfileImage(profileImage)
    }

    private fun loadExistingProfileName() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val currentName = sharedPreferences.getString("profileName", null)
            ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
            ?: "User"
        profileName.text = currentName
        usernameField.setText(currentName)

        val currentUser = FirebaseAuth.getInstance().currentUser
        emailField.setText(currentUser?.email ?: "No email available")
    }

    private fun setupListeners() {
        saveButton.setOnClickListener { saveProfileName() }
        cancelButton.setOnClickListener { finish() }
        logoutButton.setOnClickListener { logoutUser() }
        deleteAccountButton.setOnClickListener { deleteAccount() }
    }

    private fun saveProfileName() {
        val newName = usernameField.text.toString().trim()
        if (newName.isEmpty()) {
            usernameField.error = "Name cannot be empty"
            return
        }

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
                Toast.makeText(this, "Profile name updated", Toast.LENGTH_SHORT).show()
                profileName.text = username
            }
            .addOnFailureListener { exception ->
                Log.e("SettingsActivity", "Error saving username to database", exception)
                Toast.makeText(this, "Failed to update profile name", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEditor = userPrefs.edit()
        userEditor.putBoolean("isLoggedIn", false)
        userEditor.apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Clear shared preferences after account deletion
                    val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    // Logout the user and reinitialize FirebaseAuth
                    FirebaseAuth.getInstance().signOut()

                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity after account is deleted
                    val intent = Intent(this,  SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()  // Close SettingsActivity to prevent back navigation
                } else {
                    Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                    Log.e("SettingsActivity", "Error deleting account", task.exception)
                }
            }
    }

}
