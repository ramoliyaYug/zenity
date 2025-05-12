package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val currentUserId: String) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<Message>()
    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun setMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderNameTextView: TextView = itemView.findViewById(R.id.sender_name)
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text)
        private val timeTextView: TextView = itemView.findViewById(R.id.message_time)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)

        fun bind(message: Message) {
            messageTextView.text = message.content
            timeTextView.text = dateFormat.format(Date(message.timestamp))

            val isSentByMe = message.senderId == currentUserId

            // Set layout parameters for alignment
            val layoutParams = messageContainer.layoutParams as LinearLayout.LayoutParams
            if (isSentByMe) {
                layoutParams.gravity = android.view.Gravity.END
                messageContainer.setBackgroundResource(R.drawable.sent_message_background)
                senderNameTextView.visibility = View.GONE
            } else {
                layoutParams.gravity = android.view.Gravity.START
                messageContainer.setBackgroundResource(R.drawable.received_message_background)
                senderNameTextView.visibility = View.VISIBLE
                senderNameTextView.text = message.senderName
            }
            messageContainer.layoutParams = layoutParams
        }
    }
}