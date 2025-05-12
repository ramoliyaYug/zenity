package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.ForumThread
import java.text.SimpleDateFormat
import java.util.*

class ThreadAdapter(private val onThreadClick: (String, String) -> Unit) :
    RecyclerView.Adapter<ThreadAdapter.ThreadViewHolder>() {

    private val threads = mutableListOf<ForumThread>()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    fun setThreads(newThreads: List<ForumThread>) {
        threads.clear()
        threads.addAll(newThreads)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_thread, parent, false)
        return ThreadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        holder.bind(threads[position])
    }

    override fun getItemCount(): Int = threads.size

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.thread_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.thread_author)
        private val dateTextView: TextView = itemView.findViewById(R.id.thread_date)
        private val contentTextView: TextView = itemView.findViewById(R.id.thread_content)

        fun bind(thread: ForumThread) {
            titleTextView.text = thread.title
            authorTextView.text = "Posted by ${thread.authorName}"
            dateTextView.text = dateFormat.format(Date(thread.timestamp))
            contentTextView.text = thread.content

            itemView.setOnClickListener {
                onThreadClick(thread.threadId, thread.title)
            }
        }
    }
}