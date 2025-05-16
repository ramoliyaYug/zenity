package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.zenity.R
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class TherapistVerificationInfoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var infoTextView: TextView
    private lateinit var credentialsEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var skipButton: Button
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapist_verification_info)

        prefManager = PreferenceManager(this)

        // Check if user is therapist
        if (prefManager.getUserType() != "therapist") {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Verification Required"

        initViews()
        setupListeners()
    }

    private fun initViews() {
        infoTextView = findViewById(R.id.verification_info_text)
        credentialsEditText = findViewById(R.id.credentials_edit_text)
        submitButton = findViewById(R.id.submit_button)
        skipButton = findViewById(R.id.skip_button)
    }

    private fun setupListeners() {
        submitButton.setOnClickListener {
            submitVerificationRequest()
        }

        skipButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun submitVerificationRequest() {
        val credentials = credentialsEditText.text.toString().trim()

        if (credentials.isEmpty()) {
            Toast.makeText(this, "Please provide your credentials", Toast.LENGTH_SHORT).show()
            return
        }

        val therapistId = prefManager.getUserId()
        if (therapistId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        submitButton.isEnabled = false
        skipButton.isEnabled = false

        FirebaseManager.submitVerificationRequest(therapistId, credentials) { success, message ->
            if (success) {
                Toast.makeText(this, "Verification request submitted successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                submitButton.isEnabled = true
                skipButton.isEnabled = true
                Toast.makeText(this, message ?: "Failed to submit verification request", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
