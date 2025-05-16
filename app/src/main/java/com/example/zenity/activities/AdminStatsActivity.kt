package com.example.zenity.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class AdminStatsActivity : AppCompatActivity() {
    private val TAG = "AdminStatsActivity"
    private lateinit var toolbar: Toolbar
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var statsContainer: View
    private lateinit var totalPatientsTextView: TextView
    private lateinit var totalTherapistsTextView: TextView
    private lateinit var verifiedTherapistsTextView: TextView
    private lateinit var pendingVerificationsTextView: TextView
    private lateinit var totalSessionsTextView: TextView
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_stats)

        prefManager = PreferenceManager(this)

        // Check if user is admin
        if (prefManager.getUserType() != "admin") {
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Platform Statistics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadStatistics()
    }

    private fun initViews() {
        loadingAnimation = findViewById(R.id.loading_animation)
        statsContainer = findViewById(R.id.stats_container)
        totalPatientsTextView = findViewById(R.id.total_patients_value)
        totalTherapistsTextView = findViewById(R.id.total_therapists_value)
        verifiedTherapistsTextView = findViewById(R.id.verified_therapists_value)
        pendingVerificationsTextView = findViewById(R.id.pending_verifications_value)
        totalSessionsTextView = findViewById(R.id.total_sessions_value)
    }

    private fun loadStatistics() {
        loadingAnimation.visibility = View.VISIBLE
        statsContainer.visibility = View.GONE

        // Load patients count
        FirebaseManager.getAllPatients { patients ->
            totalPatientsTextView.text = patients.size.toString()
            checkAllLoaded()
        }

        // Load therapists count - using separate methods for total and verified
        FirebaseManager.getAllTherapists { therapists ->
            Log.d(TAG, "Total therapists loaded: ${therapists.size}")
            totalTherapistsTextView.text = therapists.size.toString()
            checkAllLoaded()
        }

        // Use the dedicated method for verified therapists to ensure consistency
        FirebaseManager.getVerifiedTherapists { verifiedTherapists ->
            Log.d(TAG, "Verified therapists loaded: ${verifiedTherapists.size}")
            verifiedTherapistsTextView.text = verifiedTherapists.size.toString()
            checkAllLoaded()
        }

        // Load pending verifications
        FirebaseManager.getPendingVerificationRequests { requests ->
            pendingVerificationsTextView.text = requests.size.toString()
            checkAllLoaded()
        }

        // Load sessions count (this is a simplified approach)
        FirebaseManager.database.child("sessions").get().addOnSuccessListener { snapshot ->
            totalSessionsTextView.text = snapshot.childrenCount.toString()
            checkAllLoaded()
        }.addOnFailureListener {
            totalSessionsTextView.text = "0"
            checkAllLoaded()
        }
    }

    private fun checkAllLoaded() {
        // Check if all data is loaded
        if (totalPatientsTextView.text.isNotEmpty() &&
            totalTherapistsTextView.text.isNotEmpty() &&
            verifiedTherapistsTextView.text.isNotEmpty() &&
            pendingVerificationsTextView.text.isNotEmpty() &&
            totalSessionsTextView.text.isNotEmpty()) {

            loadingAnimation.visibility = View.GONE
            statsContainer.visibility = View.VISIBLE
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
