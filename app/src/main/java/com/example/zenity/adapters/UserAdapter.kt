package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.User

class UserAdapter(private val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users: List<User> = emptyList()

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val userEmail: TextView = itemView.findViewById(R.id.user_email)
        private val userType: TextView = itemView.findViewById(R.id.user_type)
        private val verifiedIcon: ImageView = itemView.findViewById(R.id.verified_icon)

        fun bind(user: User) {
            userName.text = user.name
            userEmail.text = user.email
            userType.text = user.userType.capitalize()

            // Show verified icon for verified therapists
            if (user.userType == "therapist" && user.isVerified) {
                verifiedIcon.visibility = View.VISIBLE
            } else {
                verifiedIcon.visibility = View.GONE
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(user)
            }
        }
    }
}
