package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.User

class ChatListAdapter(private val onUserClick: (User) -> Unit) :
    RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    private val users = mutableListOf<User>()

    fun setUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_user, parent, false)
        return ChatListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class ChatListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val userTypeTextView: TextView = itemView.findViewById(R.id.user_type)

        fun bind(user: User) {
            nameTextView.text = user.name

            val userTypeText = if (user.userType == "therapist") {
                if (user.specialization.isNotEmpty()) {
                    "Therapist - ${user.specialization}"
                } else {
                    "Therapist"
                }
            } else {
                "Patient"
            }
            userTypeTextView.text = userTypeText

            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}