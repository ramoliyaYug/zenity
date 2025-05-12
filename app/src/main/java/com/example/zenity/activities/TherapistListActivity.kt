package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.adapters.TherapistAdapter
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager

class TherapistListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: TherapistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapist_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Find a Therapist"

        recyclerView = findViewById(R.id.therapist_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)

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
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadTherapists() {
        progressBar.visibility = View.VISIBLE

        FirebaseManager.getAllTherapists { therapists ->
            progressBar.visibility = View.GONE

            if (therapists.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setTherapists(therapists)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}