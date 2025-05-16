package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.models.User
import com.example.zenity.models.VerificationRequest
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TherapistVerificationDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var therapistNameTextView: TextView
    private lateinit var therapistEmailTextView: TextView
    private lateinit var therapistSpecializationTextView: TextView
    private lateinit var therapistDescriptionTextView: TextView
    private lateinit var credentialsTextView: TextView
    private lateinit var requestDateTextView: TextView
    private lateinit var adminNotesEditText: EditText
    private lateinit var approveButton: Button
    private lateinit var rejectButton: Button
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var contentScrollView: NestedScrollView
    private lateinit var prefManager: PreferenceManager

    private var requestId: String? = null
    private var therapistId: String? = null
    private var verificationRequest: VerificationRequest? = null
    private var therapist: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapist_verification_detail)

        prefManager = PreferenceManager(this)

        // Check if user is admin
        if (prefManager.getUserType() != "admin") {
            finish()
            return
        }

        requestId = intent.getStringExtra("REQUEST_ID")
        if (requestId == null) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Verification Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadVerificationRequest()
    }

    private fun initViews() {
        therapistNameTextView = findViewById(R.id.therapist_name)
        therapistEmailTextView = findViewById(R.id.therapist_email)
        therapistSpecializationTextView = findViewById(R.id.therapist_specialization)
        therapistDescriptionTextView = findViewById(R.id.therapist_description)
        credentialsTextView = findViewById(R.id.credentials_text)
        requestDateTextView = findViewById(R.id.request_date)
        adminNotesEditText = findViewById(R.id.admin_notes)
        approveButton = findViewById(R.id.approve_button)
        rejectButton = findViewById(R.id.reject_button)
        loadingAnimation = findViewById(R.id.loading_animation)
        contentScrollView = findViewById(R.id.content_scroll_view)

        approveButton.setOnClickListener {
            approveTherapist()
        }

        rejectButton.setOnClickListener {
            rejectTherapist()
        }
    }

    private fun loadVerificationRequest() {
        loadingAnimation.visibility = View.VISIBLE
        contentScrollView.visibility = View.GONE

        FirebaseManager.database.child("verificationRequests").child(requestId!!)
            .get().addOnSuccessListener { snapshot ->
                val request = snapshot.getValue(VerificationRequest::class.java)
                if (request != null) {
                    verificationRequest = request
                    therapistId = request.therapistId
                    loadTherapistProfile()
                } else {
                    Toast.makeText(this, "Verification request not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load verification request", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun loadTherapistProfile() {
        FirebaseManager.getUserProfile(therapistId!!) { user ->
            loadingAnimation.visibility = View.GONE

            if (user != null && user.userType == "therapist") {
                therapist = user
                displayVerificationDetails()
                contentScrollView.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Failed to load therapist profile", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayVerificationDetails() {
        val request = verificationRequest ?: return
        val therapist = therapist ?: return

        therapistNameTextView.text = therapist.name
        therapistEmailTextView.text = therapist.email
        therapistSpecializationTextView.text = therapist.specialization.ifEmpty { "Not specified" }
        therapistDescriptionTextView.text = therapist.description.ifEmpty { "No description provided" }
        credentialsTextView.text = request.credentials.ifEmpty { "No credentials provided" }

        // Format date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = Date(request.timestamp)
        requestDateTextView.text = "Requested on: ${dateFormat.format(date)}"
    }

    private fun approveTherapist() {
        val adminNotes = adminNotesEditText.text.toString().trim()

        approveButton.isEnabled = false
        rejectButton.isEnabled = false
        loadingAnimation.visibility = View.VISIBLE

        FirebaseManager.updateVerificationRequest(requestId!!, "approved", adminNotes) { success, message ->
            loadingAnimation.visibility = View.GONE

            if (success) {
                Toast.makeText(this, "Therapist approved successfully", Toast.LENGTH_SHORT).show()

                // After successfully approving a therapist, broadcast an intent to refresh therapist lists
                val refreshIntent = Intent("com.example.zenity.REFRESH_THERAPISTS")
                sendBroadcast(refreshIntent)

                // Also add a Toast message to confirm the action
                Toast.makeText(this, "Therapist verified successfully. They will now appear in the therapist list.", Toast.LENGTH_LONG).show()

                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                approveButton.isEnabled = true
                rejectButton.isEnabled = true
                Toast.makeText(this, message ?: "Failed to approve therapist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectTherapist() {
        val adminNotes = adminNotesEditText.text.toString().trim()

        if (adminNotes.isEmpty()) {
            Toast.makeText(this, "Please provide rejection reason", Toast.LENGTH_SHORT).show()
            return
        }

        approveButton.isEnabled = false
        rejectButton.isEnabled = false
        loadingAnimation.visibility = View.VISIBLE

        FirebaseManager.updateVerificationRequest(requestId!!, "rejected", adminNotes) { success, message ->
            loadingAnimation.visibility = View.GONE

            if (success) {
                Toast.makeText(this, "Therapist rejected", Toast.LENGTH_SHORT).show()
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                approveButton.isEnabled = true
                rejectButton.isEnabled = true
                Toast.makeText(this, message ?: "Failed to reject therapist", Toast.LENGTH_SHORT).show()
            }
        }
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
