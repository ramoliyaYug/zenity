package com.example.zenity.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenity.R
import com.example.zenity.models.ForumThread
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class NewThreadActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var postButton: Button
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_thread)

        prefManager = PreferenceManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Discussion"

        titleEditText = findViewById(R.id.thread_title_edit_text)
        contentEditText = findViewById(R.id.thread_content_edit_text)
        postButton = findViewById(R.id.post_thread_button)

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

            FirebaseManager.createForumThread(thread) { success, message ->
                postButton.isEnabled = true

                if (success) {
                    Toast.makeText(this, "Thread posted successfully", Toast.LENGTH_SHORT).show()
                    finish()
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
}