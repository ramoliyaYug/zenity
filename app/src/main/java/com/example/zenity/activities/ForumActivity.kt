package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.example.zenity.R
import com.example.zenity.adapters.ThreadAdapter
import com.example.zenity.utils.FirebaseManager

class ForumActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyView: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var newThreadButton: ExtendedFloatingActionButton
    private lateinit var adapter: ThreadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)

        supportActionBar?.hide()

        recyclerView = findViewById(R.id.threads_recycler_view)
        loadingAnimation = findViewById(R.id.loading_animation)
        emptyView = findViewById(R.id.empty_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)
        newThreadButton = findViewById(R.id.new_thread_button)

        setupRecyclerView()
        setupNewThreadButton()
        loadThreads()
    }

    private fun setupRecyclerView() {
        adapter = ThreadAdapter { threadId, threadTitle ->
            val intent = Intent(this, ThreadDetailActivity::class.java).apply {
                putExtra("THREAD_ID", threadId)
                putExtra("THREAD_TITLE", threadTitle)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupNewThreadButton() {
        newThreadButton.setOnClickListener {
            startActivity(Intent(this, NewThreadActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun loadThreads() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE

        FirebaseManager.getAllThreads { threads ->
            loadingAnimation.visibility = View.GONE

            if (threads.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setThreads(threads)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadThreads() // Refresh threads when returning to this activity
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
