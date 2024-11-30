package com.example.runapps.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.dashboard.HomeActivity
import com.example.runapps.databinding.ActivityWelcomeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Periksa apakah pengguna sudah login
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // Jika sudah login, langsung ke HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Hentikan MainActivity
            return
        }

        // Jika belum login, tampilkan halaman Welcome
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinButton.setOnClickListener {
            // Arahkan ke RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            // Arahkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
