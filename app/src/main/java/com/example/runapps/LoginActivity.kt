package com.example.runapps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.databinding.ActivityLoginEmailBinding
import com.example.runapps.databinding.ActivityLoginPasswordBinding
import com.example.runapps.databinding.ActivityLoginSuccessBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailBinding: ActivityLoginEmailBinding
    private lateinit var passwordBinding: ActivityLoginPasswordBinding
    private lateinit var successBinding: ActivityLoginSuccessBinding
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Tampilkan halaman email
        emailBinding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(emailBinding.root)

        emailBinding.nextButton.setOnClickListener {
            userEmail = emailBinding.emailEditText.text.toString()
            if (userEmail.isNotEmpty()) {
                // Lanjutkan ke halaman password
                showPasswordScreen()
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPasswordScreen() {
        // Tampilkan halaman password
        passwordBinding = ActivityLoginPasswordBinding.inflate(layoutInflater)
        setContentView(passwordBinding.root)

        passwordBinding.emailDisplay.text = userEmail
        passwordBinding.signInButton.setOnClickListener {
            val password = passwordBinding.passwordEditText.text.toString()
            if (password.isNotEmpty()) {
                // Lakukan proses login
                signIn(userEmail, password)
            } else {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Tampilkan halaman sukses
                showSuccessScreen()
            } else {
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessScreen() {
        successBinding = ActivityLoginSuccessBinding.inflate(layoutInflater)
        setContentView(successBinding.root)

        successBinding.continueButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}