package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenity.R
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class TherapistProfileActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var specializationTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var bookSessionButton: Button
    private lateinit var chatButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var prefManager: PreferenceManager

    private var therapistId: String? = null
    private var therapist: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapist_profile)

        prefManager = PreferenceManager(this)

        therapistId = intent.getStringExtra("THERAPIST_ID")
        if (therapistId == null) {
            Toast.makeText(this, "Invalid therapist ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Therapist Profile"

        initViews()
        loadTherapistProfile()
    }

    private fun initViews() {
        nameTextView = findViewById(R.id.therapist_name)
        specializationTextView = findViewById(R.id.therapist_specialization)
        descriptionTextView = findViewById(R.id.therapist_description)
        bookSessionButton = findViewById(R.id.book_session_button)
        chatButton = findViewById(R.id.chat_button)
        progressBar = findViewById(R.id.progress_bar)

        // Only show book session button for patients
        if (prefManager.getUserType() != "patient") {
            bookSessionButton.visibility = View.GONE
        }

        bookSessionButton.setOnClickListener {
            if (therapist != null) {
                val intent = Intent(this, BookingActivity::class.java).apply {
                    putExtra("THERAPIST_ID", therapistId)
                    putExtra("THERAPIST_NAME", therapist?.name)
                }
                startActivity(intent)
            }
        }

        chatButton.setOnClickListener {
            if (therapist != null) {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("OTHER_USER_ID", therapistId)
                    putExtra("OTHER_USER_NAME", therapist?.name)
                }
                startActivity(intent)
            }
        }
    }

    private fun loadTherapistProfile() {
        progressBar.visibility = View.VISIBLE

        FirebaseManager.getUserProfile(therapistId!!) { user ->
            progressBar.visibility = View.GONE

            if (user != null && user.userType == "therapist") {
                therapist = user
                displayTherapistInfo(user)
            } else {
                Toast.makeText(this, "Failed to load therapist profile", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayTherapistInfo(therapist: User) {
        nameTextView.text = therapist.name
        specializationTextView.text = therapist.specialization
        descriptionTextView.text = therapist.description

        supportActionBar?.title = therapist.name
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}