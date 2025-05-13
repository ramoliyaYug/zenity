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

    private fun loadTherapists() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        FirebaseManager.getAllTherapists { therapists ->
            loadingAnimation.visibility = View.GONE

            if (therapists.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setTherapists(therapists)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTherapists() // Refresh therapists when returning to this activity
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
