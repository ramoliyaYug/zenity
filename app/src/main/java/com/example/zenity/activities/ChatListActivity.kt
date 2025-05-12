package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.adapters.ChatListAdapter
import com.example.zenity.models.User
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: ChatListAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        prefManager = PreferenceManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Messages"

        recyclerView = findViewById(R.id.chat_list_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)

        setupRecyclerView()
        loadChatList()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter { user ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("OTHER_USER_ID", user.userId)
                putExtra("OTHER_USER_NAME", user.name)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadChatList() {
        progressBar.visibility = View.VISIBLE

        val currentUserId = prefManager.getUserId()
        if (currentUserId == null) {
            progressBar.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyView.text = "User not logged in"
            return
        }

        val userType = prefManager.getUserType()

        // For MVP, we'll just show all users of the opposite type
        // In a real app, we'd filter to show only users with existing conversations
        if (userType == "patient") {
            loadTherapists()
        } else {
            loadPatients()
        }
    }

    private fun loadTherapists() {
        FirebaseManager.getAllTherapists { therapists ->
            progressBar.visibility = View.GONE

            if (therapists.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setUsers(therapists)
            }
        }
    }

    private fun loadPatients() {
        FirebaseManager.getAllPatients { patients ->
            progressBar.visibility = View.GONE

            if (patients.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setUsers(patients)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
