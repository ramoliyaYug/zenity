package com.example.zenity.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.zenity.R
import com.example.zenity.adapters.SessionAdapter
import com.example.zenity.models.Session
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class SessionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: SessionAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        prefManager = PreferenceManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Sessions"

        recyclerView = findViewById(R.id.sessions_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)

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
        progressBar.visibility = View.VISIBLE

        val userId = prefManager.getUserId()
        if (userId == null) {
            progressBar.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
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

    private fun displaySessions(sessions: List<Session>) {
        progressBar.visibility = View.GONE

        if (sessions.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            val userType = prefManager.getUserType()
            emptyView.text = if (userType == "patient") {
                "You haven't booked any sessions yet"
            } else {
                "You don't have any upcoming sessions"
            }
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.setSessions(sessions)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}