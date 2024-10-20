package com.example.runapps

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runapps.databinding.ActivityWelcomeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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