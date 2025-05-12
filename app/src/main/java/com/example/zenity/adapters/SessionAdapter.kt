package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.Session
import com.example.zenity.utils.FirebaseManager
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(private val onSessionClick: (Session) -> Unit) :
    RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private val sessions = mutableListOf<Session>()
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    fun setSessions(newSessions: List<Session>) {
        sessions.clear()
        sessions.addAll(newSessions)
        // Sort by date (most recent first)
        sessions.sortBy { it.date }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.session_date)
        private val timeSlotTextView: TextView = itemView.findViewById(R.id.session_time_slot)
        private val sessionTypeTextView: TextView = itemView.findViewById(R.id.session_type)
        private val otherPartyTextView: TextView = itemView.findViewById(R.id.other_party_name)

        fun bind(session: Session) {
            dateTextView.text = dateFormat.format(Date(session.date))

            val timeSlot = when (session.timeSlot) {
                "morning" -> "Morning (9 AM - 12 PM)"
                "afternoon" -> "Afternoon (1 PM - 4 PM)"
                "evening" -> "Evening (5 PM - 8 PM)"
                else -> session.timeSlot.capitalize()
            }
            timeSlotTextView.text = timeSlot

            val sessionType = when (session.sessionType) {
                "initial" -> "Initial Consultation"
                "follow-up" -> "Follow-up Session"
                else -> session.sessionType.capitalize()
            }
            sessionTypeTextView.text = sessionType

            // Load the other party's name (therapist or patient)
            val otherPartyId = if (session.patientId == FirebaseManager.getCurrentUserId()) {
                session.therapistId
            } else {
                session.patientId
            }

            FirebaseManager.getUserProfile(otherPartyId) { user ->
                if (user != null) {
                    otherPartyTextView.text = "With: ${user.name}"
                } else {
                    otherPartyTextView.text = "With: Unknown User"
                }
            }

            itemView.setOnClickListener {
                onSessionClick(session)
            }
        }
    }
}