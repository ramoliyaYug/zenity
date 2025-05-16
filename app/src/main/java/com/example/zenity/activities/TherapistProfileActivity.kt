package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class TherapistProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var nameTextView: TextView
    private lateinit var specializationTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var bookSessionButton: Button
    private lateinit var chatButton: Button
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var profileContent: NestedScrollView
    private lateinit var prefManager: PreferenceManager
    private lateinit var verificationBadge: View
    private lateinit var verificationStatus: TextView

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

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Therapist Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Find verification views - using the correct IDs from the layout
        verificationBadge = findViewById(R.id.verification_badge)
        verificationStatus = findViewById(R.id.therapist_specialization) // Using specialization TextView to show verification status

        initViews()
        loadTherapistProfile()
    }

    private fun initViews() {
        nameTextView = findViewById(R.id.therapist_name)
        specializationTextView = findViewById(R.id.therapist_specialization)
        descriptionTextView = findViewById(R.id.therapist_description)
        bookSessionButton = findViewById(R.id.book_session_button)
        chatButton = findViewById(R.id.chat_button)
        loadingAnimation = findViewById(R.id.progress_bar)
        profileContent = findViewById(R.id.profile_content)

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
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        chatButton.setOnClickListener {
            if (therapist != null) {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("OTHER_USER_ID", therapistId)
                    putExtra("OTHER_USER_NAME", therapist?.name)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun loadTherapistProfile() {
        loadingAnimation.visibility = View.VISIBLE
        profileContent.visibility = View.GONE

        FirebaseManager.getUserProfile(therapistId!!) { user ->
            loadingAnimation.visibility = View.GONE

            if (user != null && user.userType == "therapist") {
                therapist = user
                displayTherapistInfo(user)
                profileContent.visibility = View.VISIBLE

                // Update verification status
                if (user.isVerified) {
                    // Show verification badge and update specialization text to include verification
                    verificationBadge.visibility = View.VISIBLE
                    specializationTextView.text = "${user.specialization ?: "General Therapy"} • Verified Professional"
                    specializationTextView.setTextColor(resources.getColor(R.color.colorSuccess, null))

                    // Log verification status for debugging
                    android.util.Log.d("TherapistProfile", "Therapist ${user.name} is verified")
                } else {
                    // Hide verification badge
                    verificationBadge.visibility = View.GONE
                    specializationTextView.text = user.specialization ?: "General Therapy"
                    specializationTextView.setTextColor(resources.getColor(R.color.colorPrimary, null))

                    // Log verification status for debugging
                    android.util.Log.d("TherapistProfile", "Therapist ${user.name} is NOT verified")
                }
            } else {
                Toast.makeText(this, "Failed to load therapist profile", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayTherapistInfo(therapist: User) {
        nameTextView.text = therapist.name
        specializationTextView.text = therapist.specialization ?: "General Therapy"
        descriptionTextView.text = therapist.description ?: "No description available"

        supportActionBar?.title = therapist.name
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
