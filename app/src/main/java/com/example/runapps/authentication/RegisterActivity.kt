package com.example.runapps.authentication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.HomeActivity
import com.example.runapps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val togglePasswordVisibility = findViewById<ImageView>(R.id.togglePasswordVisibility)
        val dobEditText = findViewById<EditText>(R.id.dobEditText)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)

        // Toggle password visibility
        togglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordVisibility.setImageResource(R.drawable.hideeye)
                isPasswordVisible = false
            } else {
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePasswordVisibility.setImageResource(R.drawable.openeye)
                isPasswordVisible = true
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Show DatePickerDialog when date of birth EditText is clicked
        dobEditText.setOnClickListener {
            showDatePickerDialog(dobEditText)
        }

        // Handle account creation
        createAccountButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                createAccount(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog(dobEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create DatePickerDialog and limit the date to the current or past dates
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set the selected date in the EditText
                dobEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            },
            year, month, day
        )

        // Limit the date picker to past dates only
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Account creation successful
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "email" to email,
                            "uid" to it.uid
                        )

                        firestore.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Account creation failed
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthWeakPasswordException -> {
                            Toast.makeText(this, "Weak password. Must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(this, "Email already registered.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Account creation failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
}