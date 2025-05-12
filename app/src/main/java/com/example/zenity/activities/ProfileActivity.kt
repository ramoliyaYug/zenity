package com.example.zenity.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenity.R
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var userTypeTextView: TextView
    private lateinit var descriptionEditText: EditText
    private lateinit var specializationEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var prefManager: PreferenceManager

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefManager = PreferenceManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"

        initViews()
        loadUserProfile()
    }

    private fun initViews() {
        nameEditText = findViewById(R.id.name_edit_text)
        emailTextView = findViewById(R.id.email_text_view)
        userTypeTextView = findViewById(R.id.user_type_text_view)
        descriptionEditText = findViewById(R.id.description_edit_text)
        specializationEditText = findViewById(R.id.specialization_edit_text)
        ageEditText = findViewById(R.id.age_edit_text)
        saveButton = findViewById(R.id.save_button)

        // Show/hide fields based on user type
        val userType = prefManager.getUserType()
        if (userType == "patient") {
            findViewById<TextView>(R.id.specialization_label).visibility = android.view.View.GONE
            specializationEditText.visibility = android.view.View.GONE
        } else {
            findViewById<TextView>(R.id.age_label).visibility = android.view.View.GONE
            ageEditText.visibility = android.view.View.GONE
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        val userId = prefManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseManager.getUserProfile(userId) { user ->
            if (user != null) {
                currentUser = user
                displayUserInfo(user)
            } else {
                Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserInfo(user: User) {
        nameEditText.setText(user.name)
        emailTextView.text = user.email
        userTypeTextView.text = user.userType.capitalize()
        descriptionEditText.setText(user.description)

        if (user.userType == "therapist") {
            specializationEditText.setText(user.specialization)
        } else {
            user.age?.let { ageEditText.setText(it.toString()) }
        }
    }

    private fun saveProfile() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = prefManager.getUserId() ?: return
        val userType = prefManager.getUserType() ?: return
        val email = currentUser?.email ?: return

        val updatedUser = if (userType == "therapist") {
            val specialization = specializationEditText.text.toString().trim()
            User(
                userId = userId,
                email = email,
                name = name,
                userType = userType,
                description = description,
                specialization = specialization
            )
        } else {
            val ageText = ageEditText.text.toString().trim()
            val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null
            User(
                userId = userId,
                email = email,
                name = name,
                userType = userType,
                description = description,
                age = age
            )
        }

        FirebaseManager.saveUserProfile(updatedUser) { success, message ->
            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                prefManager.saveUserSession(userId, userType, name)
                finish()
            } else {
                Toast.makeText(this, message ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}