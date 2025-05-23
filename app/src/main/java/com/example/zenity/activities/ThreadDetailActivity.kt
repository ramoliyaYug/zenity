package com.example.zenity.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.adapters.ReplyAdapter
import com.example.zenity.models.ForumThread
import com.example.zenity.models.ThreadReply
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText

class ThreadDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var threadTitleTextView: TextView
    private lateinit var threadAuthorTextView: TextView
    private lateinit var threadDateTextView: TextView
    private lateinit var threadContentTextView: TextView
    private lateinit var repliesRecyclerView: RecyclerView
    private lateinit var replyEditText: TextInputEditText
    private lateinit var sendReplyButton: Button
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var contentContainer: View
    private lateinit var emptyRepliesView: TextView
    private lateinit var adapter: ReplyAdapter
    private lateinit var prefManager: PreferenceManager

    private var threadId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread_detail)

        prefManager = PreferenceManager(this)

        threadId = intent.getStringExtra("THREAD_ID")
        val threadTitle = intent.getStringExtra("THREAD_TITLE")

        if (threadId == null) {
            Toast.makeText(this, "Invalid thread ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = threadTitle ?: "Thread Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupRecyclerView()
        loadThreadDetails()
        loadReplies()
        setupSendReplyButton()
    }

    private fun initViews() {
        threadTitleTextView = findViewById(R.id.thread_title)
        threadAuthorTextView = findViewById(R.id.thread_author)
        threadDateTextView = findViewById(R.id.thread_date)
        threadContentTextView = findViewById(R.id.thread_content)
        repliesRecyclerView = findViewById(R.id.replies_recycler_view)
        replyEditText = findViewById(R.id.reply_edit_text)
        sendReplyButton = findViewById(R.id.send_reply_button)
        loadingAnimation = findViewById(R.id.progress_bar)
        contentContainer = findViewById(R.id.content_container)
        emptyRepliesView = findViewById(R.id.empty_replies_view)
    }

    private fun setupRecyclerView() {
        adapter = ReplyAdapter()
        repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        repliesRecyclerView.adapter = adapter
    }

    private fun loadThreadDetails() {
        loadingAnimation.visibility = View.VISIBLE
        contentContainer.visibility = View.GONE

        FirebaseManager.database.child("threads").child(threadId!!)
            .get().addOnSuccessListener { snapshot ->
                val thread = snapshot.getValue(ForumThread::class.java)
                if (thread != null) {
                    displayThreadDetails(thread)
                    contentContainer.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Thread not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load thread details", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayThreadDetails(thread: ForumThread) {
        threadTitleTextView.text = thread.title
        threadAuthorTextView.text = "Posted by ${thread.authorName}"

        val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
        threadDateTextView.text = dateFormat.format(java.util.Date(thread.timestamp))

        threadContentTextView.text = thread.content
    }

    private fun loadReplies() {
        FirebaseManager.getThreadReplies(threadId!!) { replies ->
            loadingAnimation.visibility = View.GONE

            if (replies.isEmpty()) {
                emptyRepliesView.visibility = View.VISIBLE
                repliesRecyclerView.visibility = View.GONE
            } else {
                emptyRepliesView.visibility = View.GONE
                repliesRecyclerView.visibility = View.VISIBLE
                adapter.setReplies(replies)
            }
        }
    }

    private fun setupSendReplyButton() {
        sendReplyButton.setOnClickListener {
            val replyContent = replyEditText.text.toString().trim()
            if (replyContent.isEmpty()) {
                Toast.makeText(this, "Reply cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = prefManager.getUserId()
            val userName = prefManager.getUserName()

            if (userId == null || userName == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reply = ThreadReply(
                threadId = threadId!!,
                content = replyContent,
                authorId = userId,
                authorName = userName,
                timestamp = System.currentTimeMillis()
            )

            sendReplyButton.isEnabled = false

            FirebaseManager.addReplyToThread(reply) { success, message ->
                sendReplyButton.isEnabled = true

                if (success) {
                    replyEditText.text?.clear()
                    loadReplies() // Refresh replies
                } else {
                    Toast.makeText(this, message ?: "Failed to send reply", Toast.LENGTH_SHORT).show()
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
