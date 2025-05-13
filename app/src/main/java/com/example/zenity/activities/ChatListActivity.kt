package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.zenity.R
import com.example.zenity.adapters.ChatListAdapter
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyView: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var adapter: ChatListAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        prefManager = PreferenceManager(this)

        supportActionBar?.hide()

        recyclerView = findViewById(R.id.chat_list_recycler_view)
        loadingAnimation = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)

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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadChatList() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        val currentUserId = prefManager.getUserId()
        if (currentUserId == null) {
            loadingAnimation.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
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
            loadingAnimation.visibility = View.GONE

            if (therapists.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setUsers(therapists)
            }
        }
    }

    private fun loadPatients() {
        FirebaseManager.getAllPatients { patients ->
            loadingAnimation.visibility = View.GONE

            if (patients.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setUsers(patients)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadChatList() // Refresh chat list when returning to this activity
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
