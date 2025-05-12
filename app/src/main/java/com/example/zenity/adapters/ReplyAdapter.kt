package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.ThreadReply
import java.text.SimpleDateFormat
import java.util.*

class ReplyAdapter : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    private val replies = mutableListOf<ThreadReply>()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    fun setReplies(newReplies: List<ThreadReply>) {
        replies.clear()
        replies.addAll(newReplies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(replies[position])
    }

    override fun getItemCount(): Int = replies.size

    inner class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorTextView: TextView = itemView.findViewById(R.id.reply_author)
        private val dateTextView: TextView = itemView.findViewById(R.id.reply_date)
        private val contentTextView: TextView = itemView.findViewById(R.id.reply_content)

        fun bind(reply: ThreadReply) {
            authorTextView.text = reply.authorName
            dateTextView.text = dateFormat.format(Date(reply.timestamp))
            contentTextView.text = reply.content
        }
    }
}