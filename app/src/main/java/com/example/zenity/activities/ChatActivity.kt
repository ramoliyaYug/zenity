package com.example.zenity.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.example.zenity.R
import com.example.zenity.adapters.MessageAdapter
import com.example.zenity.models.Message
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: MessageAdapter
    private lateinit var prefManager: PreferenceManager

    private var otherUserId: String? = null
    private var otherUserName: String? = null
    private var messagesListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        prefManager = PreferenceManager(this)

        otherUserId = intent.getStringExtra("OTHER_USER_ID")
        otherUserName = intent.getStringExtra("OTHER_USER_NAME")

        if (otherUserId == null || otherUserName == null) {
            Toast.makeText(this, "Invalid chat parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.title = otherUserName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.messages_recycler_view)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)

        setupRecyclerView()
        setupMessageListener()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(prefManager.getUserId() ?: "")
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter
    }

    private fun setupMessageListener() {
        val currentUserId = prefManager.getUserId() ?: return

        messagesListener = FirebaseManager.getMessages(currentUserId, otherUserId!!) { messages ->
            adapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isEmpty()) return@setOnClickListener

            val currentUserId = prefManager.getUserId() ?: return@setOnClickListener
            val currentUserName = prefManager.getUserName() ?: return@setOnClickListener

            val message = Message(
                senderId = currentUserId,
                receiverId = otherUserId!!,
                senderName = currentUserName,
                content = messageText,
                timestamp = System.currentTimeMillis()
            )

            FirebaseManager.sendMessage(message) { success, _ ->
                if (success) {
                    messageInput.text.clear()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.let {
            FirebaseManager.database.child("messages").removeEventListener(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}