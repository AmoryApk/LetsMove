package com.example.runapps.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runapps.activity.MapsActivity
import com.example.runapps.profile.ProfilePage
import com.example.runapps.R
import com.example.runapps.activity.RecentActivity
import com.example.runapps.activity.TrackingData
import com.example.runapps.authentication.RecentActivityAdapter
import com.example.runapps.starter.StarterService
import com.example.runapps.starter.StarterService.calculateCalories
import com.example.runapps.starter.StarterService.calculatePace
import com.example.runapps.starter.StarterService.convertMeterToKm
import com.example.runapps.starter.StarterService.convertSecondToHour
import com.example.runapps.starter.StarterService.formatDateToReadable
import com.example.runapps.starter.StarterService.loadProfileImage
import com.example.runapps.starter.StarterService.loadProfileName
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.getValue
import kotlin.text.format
import java.text.DecimalFormat
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

class HomeActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private lateinit var recentActivityRecyclerView: RecyclerView
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var recentActivityList: ArrayList<RecentActivity>
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var welcomeTextView: TextView // Declare the TextView for welcome message

    private lateinit var weeklyGoalText: TextView
    private lateinit var weeklyGoalProgressText: TextView
    private lateinit var weeklyGoalLeftText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var profileImage: ImageView // Declare the ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView) // Initialize the TextView
        weeklyGoalText = findViewById(R.id.weeklyGoalText)
        weeklyGoalProgressText = findViewById(R.id.weeklyGoalProgressText)
        weeklyGoalLeftText = findViewById(R.id.weeklyGoalLeftText)
        progressBar = findViewById(R.id.myProgressBar)
        profileImage = findViewById(R.id.profileImage)

        loadProfileImage(profileImage)

        // Initialize RecyclerView
        recentActivityRecyclerView = findViewById(R.id.recentActivityRecyclerView)
        recentActivityRecyclerView.layoutManager = LinearLayoutManager(this)
        recentActivityRecyclerView.setHasFixedSize(true)

        // Initialize Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Load initial sample data for recent activities
//        loadSampleData()
        fetchRecentActivityData { recentActivityList ->
            // Use the recentActivityList here
            recentActivityAdapter = RecentActivityAdapter(recentActivityList)
            recentActivityRecyclerView.adapter = recentActivityAdapter
        }

        // Initialize Adapter and set it to RecyclerView
//        recentActivityAdapter = RecentActivityAdapter(recentActivityList)
//        recentActivityRecyclerView.adapter = recentActivityAdapter

        // Set welcome message with user name
        if (currentUser != null) {
            var username = loadProfileName(welcomeTextView)
            welcomeTextView.text = "Hello, $username" // Set the welcome message
        }

        // Set up Bottom Navigation item selected listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle Home navigation
                    true
                }
                R.id.navigation_activity -> {
                    // Handle Activity navigation
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
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

    private fun fetchRecentActivityData(callback: (ArrayList<RecentActivity>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val recentActivityRef = database.reference.child("user_tracking").child(userId).child("trackingData")
            recentActivityRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val recentActivityList = fetchRecentActivities(dataSnapshot)
                    updateWeeklyGoalProgress(recentActivityList)
                    callback(recentActivityList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    handleFetchError(databaseError, callback)
                }
            })
        } else {
            handleUserNotLoggedIn(callback)
        }
    }

    private fun fetchRecentActivities(dataSnapshot: DataSnapshot): ArrayList<RecentActivity> {
        val recentActivityList = ArrayList<RecentActivity>()
        var totalDistance = 0.0
        val totalTarget = convertMeterToKm(5000.0)

        for (snapshot in dataSnapshot.children) {
            val trackingData = snapshot.getValue(TrackingData::class.java)
            if (trackingData != null) {
                val distance = convertMeterToKm(trackingData.distance.toDouble())
                val calories = calculateCalories(trackingData.distance.toDouble(), trackingData.pace.toDouble())
                val pace = calculatePace(distance, convertSecondToHour(trackingData.runningTime.toDouble()))

                val recentActivity = createRecentActivity(trackingData, distance, calories, pace)
                recentActivityList.add(recentActivity)

                totalDistance += distance
            }
        }

        return recentActivityList
    }

    private fun createRecentActivity(trackingData: TrackingData, distance: Double, calories: Double, pace: Double): RecentActivity {
        val decimalFormat = DecimalFormat("0.###")
        val distanceFormatted = decimalFormat.format(distance)
        val caloriesFormatted = decimalFormat.format(calories)
        val paceFormatted = decimalFormat.format(pace)

        return RecentActivity(
            trackingData.runningDate,
            formatDateToReadable(trackingData.runningDate),
            distanceFormatted + " Km",
            caloriesFormatted,
            paceFormatted,
            trackingData.step.toString()
        )
    }

    private fun updateWeeklyGoalProgress(recentActivityList: ArrayList<RecentActivity>) {
        var totalDistance = 0.0
        val totalTarget = 5000.0
        val today = Calendar.getInstance().time
        val todayCalendar = Calendar.getInstance()
        todayCalendar.time = today
        val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)
        // Filter out activities that are older than the current week
        val filteredActivities = recentActivityList.filter { activity ->
            val activityDateString = activity.runningDate
            val activityDate = LocalDate.parse(activityDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val activityCalendar = Calendar.getInstance()
            activityCalendar.time = Date.from(activityDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val activityDayOfWeek = activityCalendar.get(Calendar.DAY_OF_WEEK)
            val daysBetween = todayDayOfWeek - activityDayOfWeek
            daysBetween <= 7 // Only include activities from the current week
        }
        for (activity in filteredActivities) {
            val distance = activity.distance.replace(" Km", "").toDouble()
            totalDistance += distance
        }

        val remainingDistance = convertMeterToKm(totalTarget) - totalDistance
        val progressPercentage = ((totalDistance / totalTarget) * 100).toInt()

        progressBar.progress = progressPercentage
        weeklyGoalText.text = DecimalFormat("0.###").format(convertMeterToKm(totalTarget)) + " Km"
        weeklyGoalProgressText.text = DecimalFormat("0.###").format(totalDistance) + " Km"
        weeklyGoalLeftText.text = DecimalFormat("0.###").format(remainingDistance) + " Km"
    }

    private fun handleFetchError(databaseError: DatabaseError, callback: (ArrayList<RecentActivity>) -> Unit) {
        Log.e("HomeActivity", "Error fetching recent activity data: ${databaseError.message}", databaseError.toException())
        callback(ArrayList())
    }

    private fun handleUserNotLoggedIn(callback: (ArrayList<RecentActivity>) -> Unit) {
        callback(ArrayList())
    }



}