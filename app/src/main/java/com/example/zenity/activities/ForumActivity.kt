package com.example.zenity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.zenity.R
import com.example.zenity.adapters.ThreadAdapter
import com.example.zenity.utils.FirebaseManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ForumActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var newThreadButton: ExtendedFloatingActionButton
    private lateinit var adapter: ThreadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)

        supportActionBar?.title = "Community Forum"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.threads_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
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
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupNewThreadButton() {
        newThreadButton.setOnClickListener {
            startActivity(Intent(this, NewThreadActivity::class.java))
        }
    }

    private fun loadThreads() {
        progressBar.visibility = View.VISIBLE

        FirebaseManager.getAllThreads { threads ->
            progressBar.visibility = View.GONE

            if (threads.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
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
}