package com.example.zenity.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.models.ForumThread
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText

class NewThreadActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var titleEditText: TextInputEditText
    private lateinit var contentEditText: TextInputEditText
    private lateinit var postButton: Button
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_thread)

        prefManager = PreferenceManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "New Discussion"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleEditText = findViewById(R.id.thread_title_edit_text)
        contentEditText = findViewById(R.id.thread_content_edit_text)
        postButton = findViewById(R.id.post_thread_button)
        loadingAnimation = findViewById(R.id.loading_animation)

        setupPostButton()
    }

    private fun setupPostButton() {
        postButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.isEmpty()) {
                Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = prefManager.getUserId()
            val userName = prefManager.getUserName()

            if (userId == null || userName == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val thread = ForumThread(
                title = title,
                content = content,
                authorId = userId,
                authorName = userName,
                timestamp = System.currentTimeMillis()
            )

            postButton.isEnabled = false
            loadingAnimation.visibility = View.VISIBLE

            FirebaseManager.createForumThread(thread) { success, message ->
                loadingAnimation.visibility = View.GONE
                postButton.isEnabled = true

                if (success) {
                    Toast.makeText(this, "Thread posted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                } else {
                    Toast.makeText(this, message ?: "Failed to post thread", Toast.LENGTH_SHORT).show()
                }
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
