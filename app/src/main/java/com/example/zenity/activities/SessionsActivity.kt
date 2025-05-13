package com.example.zenity.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.adapters.SessionAdapter
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class SessionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyView: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var adapter: SessionAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        prefManager = PreferenceManager(this)

        supportActionBar?.hide()

        recyclerView = findViewById(R.id.sessions_recycler_view)
        loadingAnimation = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)

        setupRecyclerView()
        loadSessions()
    }

    private fun setupRecyclerView() {
        adapter = SessionAdapter { session ->
            // Handle session click if needed
            // For MVP, we're not implementing session details
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadSessions() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        val userId = prefManager.getUserId()
        if (userId == null) {
            loadingAnimation.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
            emptyView.text = "User not logged in"
            return
        }

        val userType = prefManager.getUserType()

        // Load sessions based on user type
        if (userType == "patient") {
            loadPatientSessions(userId)
        } else {
            loadTherapistSessions(userId)
        }
    }

    private fun loadPatientSessions(patientId: String) {
        FirebaseManager.getUserSessions(patientId) { sessions ->
            displaySessions(sessions)
        }
    }

    private fun loadTherapistSessions(therapistId: String) {
        FirebaseManager.getTherapistSessions(therapistId) { sessions ->
            displaySessions(sessions)
        }
    }

    private fun displaySessions(sessions: List<com.example.zenity.models.Session>) {
        loadingAnimation.visibility = View.GONE

        if (sessions.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            val userType = prefManager.getUserType()
            emptyView.text = if (userType == "patient") {
                "You haven't booked any sessions yet"
            } else {
                "You don't have any upcoming sessions"
            }
        } else {
            emptyStateContainer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.setSessions(sessions)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSessions() // Refresh sessions when returning to this activity
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
