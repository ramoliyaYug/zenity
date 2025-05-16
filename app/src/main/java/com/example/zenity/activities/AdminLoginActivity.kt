package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.zenity.R
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        prefManager = PreferenceManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Admin Login"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emailEditText = findViewById(R.id.admin_email)
        passwordEditText = findViewById(R.id.admin_password)
        loginButton = findViewById(R.id.admin_login_button)

        loginButton.setOnClickListener {
            loginAsAdmin()
        }
    }

    private fun loginAsAdmin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseManager.loginUser(email, password) { success, message ->
            if (success) {
                val userId = FirebaseManager.getCurrentUserId()
                if (userId != null) {
                    FirebaseManager.getUserProfile(userId) { user ->
                        if (user != null && user.userType == "admin") {
                            prefManager.saveUserSession(userId, user.userType, user.name)
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "This account is not an admin account", Toast.LENGTH_SHORT).show()
                            FirebaseManager.logout()
                        }
                    }
                }
            } else {
                Toast.makeText(this, message ?: "Login failed", Toast.LENGTH_SHORT).show()
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
