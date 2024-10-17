package com.example.runapps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.runapps.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var auth: FirebaseAuth  // Tambahkan deklarasi auth

    private val presenter = MapPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Dapatkan SupportMapFragment dan dapatkan pemberitahuan ketika peta siap digunakan
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnStartStop.setOnClickListener {
            if (binding.btnStartStop.text == getString(R.string.start_label)) {
                startTracking()
                binding.btnStartStop.setText(R.string.stop_label)
            } else {
                stopTracking()
                binding.btnStartStop.setText(R.string.start_label)
            }
        }

        presenter.onViewCreated()

        // Set up Bottom Navigation item selected listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle Home navigation
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_activity -> {
                    // Handle Activity navigation
                    true // Do nothing as we are already in MapsActivity
                }
                R.id.navigation_profile -> {
                    // Handle Profile navigation
                    startActivity(Intent(this, ProfilePage::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        presenter.ui.observe(this) { ui ->
            updateUi(ui)
        }

        presenter.onMapLoaded()
        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun startTracking() {
        binding.container.txtPace.text = ""
        binding.container.txtDistance.text = ""
        binding.container.txtTime.base = SystemClock.elapsedRealtime()
        binding.container.txtTime.start()
        map.clear()

        presenter.startTracking()
    }

    private fun stopTracking() {
        presenter.stopTracking()
        binding.container.txtTime.stop()
    }

    @SuppressLint("MissingPermission")
    private fun updateUi(ui: Ui) {
        if (ui.currentLocation != null && ui.currentLocation != map.cameraPosition.target) {
            map.isMyLocationEnabled = true
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(ui.currentLocation, 14f))
        }
        binding.container.txtDistance.text = ui.formattedDistance
        binding.container.txtPace.text = ui.formattedPace
        drawRoute(ui.userPath)
    }

    private fun drawRoute(locations: List<LatLng>) {
        val polylineOptions = PolylineOptions()

        map.clear()

        val points = polylineOptions.points
        points.addAll(locations)

        map.addPolyline(polylineOptions)
    }
}