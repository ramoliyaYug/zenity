package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.adapters.TherapistAdapter
import com.example.zenity.utils.FirebaseManager

class TherapistListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyView: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var adapter: TherapistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapist_list)

        supportActionBar?.hide()

        recyclerView = findViewById(R.id.therapist_recycler_view)
        loadingAnimation = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)

        setupRecyclerView()
        loadTherapists()
    }

    private fun setupRecyclerView() {
        adapter = TherapistAdapter { therapist ->
            // Handle therapist selection
            val intent = Intent(this, TherapistProfileActivity::class.java).apply {
                putExtra("THERAPIST_ID", therapist.userId)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // Update the loadTherapists method to ensure it's refreshing properly
    // Replace the existing loadTherapists method with this improved version:

    private fun loadTherapists() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        // Add logging to help diagnose the issue
        FirebaseManager.getVerifiedTherapists { therapists ->
            loadingAnimation.visibility = View.GONE

            // Log the number of therapists received
            android.util.Log.d("TherapistListActivity", "Received ${therapists.size} verified therapists")

            if (therapists.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                emptyView.text = "No verified therapists available at the moment."
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setTherapists(therapists)
            }
        }
    }

    // Add a method to force refresh the therapist list
    // Add this new method after the loadTherapists method:

    // Enhanced force refresh method with better error handling and logging
    private fun forceRefreshTherapists() {
        // Clear any adapter data first
        adapter.setTherapists(emptyList())

        // Show loading state
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        android.util.Log.d("TherapistListActivity", "Starting force refresh of therapist list")

        // Add a small delay to ensure Firebase has the latest data
        recyclerView.postDelayed({
            FirebaseManager.getVerifiedTherapists { therapists ->
                loadingAnimation.visibility = View.GONE

                android.util.Log.d("TherapistListActivity", "Force refresh: Received ${therapists.size} verified therapists")

                // Log each therapist for debugging
                therapists.forEach { therapist ->
                    android.util.Log.d("TherapistListActivity",
                        "Therapist: ${therapist.name}, ID: ${therapist.userId}, Verified: ${therapist.isVerified}")
                }

                if (therapists.isEmpty()) {
                    emptyStateContainer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    emptyView.text = "No verified therapists available at the moment."
                    android.util.Log.w("TherapistListActivity", "No verified therapists found")
                } else {
                    emptyStateContainer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.setTherapists(therapists)
                    android.util.Log.d("TherapistListActivity", "Successfully displayed ${therapists.size} therapists")
                }
            }
        }, 1500) // 1.5 second delay to ensure Firebase has latest data
    }

    // Update the onResume method to use the force refresh method
    // Replace the existing onResume method with this improved version:

    override fun onResume() {
        super.onResume()
        // Use force refresh instead of regular load to ensure we get the latest data
        forceRefreshTherapists()
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
