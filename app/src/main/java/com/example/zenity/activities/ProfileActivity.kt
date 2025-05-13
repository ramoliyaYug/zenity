package com.example.zenity.activities

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
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailTextView: TextView
    private lateinit var userTypeTextView: TextView
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var specializationEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var specializationLayout: TextInputLayout
    private lateinit var ageLayout: TextInputLayout
    private lateinit var saveButton: Button
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var profileContent: NestedScrollView
    private lateinit var prefManager: PreferenceManager

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefManager = PreferenceManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        specializationLayout = findViewById(R.id.specialization_layout)
        ageLayout = findViewById(R.id.age_layout)
        saveButton = findViewById(R.id.save_button)
        loadingAnimation = findViewById(R.id.loading_animation)
        profileContent = findViewById(R.id.profile_content)

        // Show/hide fields based on user type
        val userType = prefManager.getUserType()
        if (userType == "patient") {
            specializationLayout.visibility = View.GONE
        } else {
            ageLayout.visibility = View.GONE
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        loadingAnimation.visibility = View.VISIBLE
        profileContent.visibility = View.GONE

        val userId = prefManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseManager.getUserProfile(userId) { user ->
            loadingAnimation.visibility = View.GONE

            if (user != null) {
                currentUser = user
                displayUserInfo(user)
                profileContent.visibility = View.VISIBLE
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

        saveButton.isEnabled = false
        loadingAnimation.visibility = View.VISIBLE

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
            loadingAnimation.visibility = View.GONE
            saveButton.isEnabled = true

            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                prefManager.saveUserSession(userId, userType, name)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                Toast.makeText(this, message ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
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
