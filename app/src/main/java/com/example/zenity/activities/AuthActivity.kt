package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenity.R
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class AuthActivity : AppCompatActivity() {

    private lateinit var prefManager: PreferenceManager

    // Login views
    private lateinit var loginLayout: View
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var switchToSignup: TextView

    // Signup views
    private lateinit var signupLayout: View
    private lateinit var signupName: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var signupButton: Button
    private lateinit var switchToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        prefManager = PreferenceManager(this)

        // Check if user is already logged in
        if (prefManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // Login views
        loginLayout = findViewById(R.id.login_layout)
        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        switchToSignup = findViewById(R.id.switch_to_signup)

        // Signup views
        signupLayout = findViewById(R.id.signup_layout)
        signupName = findViewById(R.id.signup_name)
        signupEmail = findViewById(R.id.signup_email)
        signupPassword = findViewById(R.id.signup_password)
        userTypeGroup = findViewById(R.id.user_type_group)
        signupButton = findViewById(R.id.signup_button)
        switchToLogin = findViewById(R.id.switch_to_login)

        // Initially show login
        loginLayout.visibility = View.VISIBLE
        signupLayout.visibility = View.GONE
    }

    private fun setupListeners() {
        switchToSignup.setOnClickListener {
            loginLayout.visibility = View.GONE
            signupLayout.visibility = View.VISIBLE
        }

        switchToLogin.setOnClickListener {
            signupLayout.visibility = View.GONE
            loginLayout.visibility = View.VISIBLE
        }

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseManager.loginUser(email, password) { success, message ->
                if (success) {
                    val userId = FirebaseManager.getCurrentUserId()
                    if (userId != null) {
                        FirebaseManager.getUserProfile(userId) { user ->
                            if (user != null) {
                                prefManager.saveUserSession(userId, user.userType, user.name)
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to get user profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signupButton.setOnClickListener {
            val name = signupName.text.toString().trim()
            val email = signupEmail.text.toString().trim()
            val password = signupPassword.text.toString().trim()
            val userType = if (userTypeGroup.checkedRadioButtonId == R.id.radio_patient) "patient" else "therapist"

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseManager.registerUser(email, password) { success, userId ->
                if (success && userId != null) {
                    val user = User(
                        userId = userId,
                        email = email,
                        name = name,
                        userType = userType
                    )

                    FirebaseManager.saveUserProfile(user) { profileSuccess, profileMessage ->
                        if (profileSuccess) {
                            prefManager.saveUserSession(userId, userType, name)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, profileMessage ?: "Failed to save profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, userId ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}