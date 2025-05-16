package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.adapters.VerificationRequestAdapter
import com.example.zenity.models.VerificationRequest
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class TherapistVerificationListActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyView: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var adapter: VerificationRequestAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapist_verification_list)

        prefManager = PreferenceManager(this)

        // Check if user is admin
        if (prefManager.getUserType() != "admin") {
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Therapist Verification"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.verification_recycler_view)
        loadingAnimation = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)

        setupRecyclerView()
        loadVerificationRequests()
    }

    private fun setupRecyclerView() {
        adapter = VerificationRequestAdapter { request ->
            // Handle verification request selection
            val intent = Intent(this, TherapistVerificationDetailActivity::class.java).apply {
                putExtra("REQUEST_ID", request.requestId)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadVerificationRequests() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        FirebaseManager.getPendingVerificationRequests { requests ->
            loadingAnimation.visibility = View.GONE

            if (requests.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                emptyView.text = "No pending verification requests"
                recyclerView.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setVerificationRequests(requests)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadVerificationRequests() // Refresh requests when returning to this activity
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
