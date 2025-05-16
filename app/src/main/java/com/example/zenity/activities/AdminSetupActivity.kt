package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenity.R
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class AdminSetupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_setup)

        prefManager = PreferenceManager(this)

        nameEditText = findViewById(R.id.admin_name)
        emailEditText = findViewById(R.id.admin_email)
        passwordEditText = findViewById(R.id.admin_password)
        confirmPasswordEditText = findViewById(R.id.admin_confirm_password)
        createButton = findViewById(R.id.create_admin_button)
        progressBar = findViewById(R.id.progress_bar)

        createButton.setOnClickListener {
            createAdminAccount()
        }
    }

    private fun createAdminAccount() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        progressBar.visibility = View.VISIBLE
        createButton.isEnabled = false

        // Create admin account
        FirebaseManager.createAdminAccount(email, password, name) { success, message ->
            progressBar.visibility = View.GONE
            createButton.isEnabled = true

            if (success) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Login as admin
                FirebaseManager.loginUser(email, password) { loginSuccess, _ ->
                    if (loginSuccess) {
                        val userId = FirebaseManager.getCurrentUserId()
                        if (userId != null) {
                            prefManager.saveUserSession(userId, "admin", name)
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Admin created but login failed. Please login manually.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, AdminLoginActivity::class.java))
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, message ?: "Failed to create admin account", Toast.LENGTH_LONG).show()
            }
        }
    }
}
