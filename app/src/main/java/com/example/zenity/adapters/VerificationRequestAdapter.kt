package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.VerificationRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VerificationRequestAdapter(private val onItemClick: (VerificationRequest) -> Unit) :
    RecyclerView.Adapter<VerificationRequestAdapter.VerificationRequestViewHolder>() {

    private var requests: List<VerificationRequest> = emptyList()

    fun setVerificationRequests(requests: List<VerificationRequest>) {
        this.requests = requests
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerificationRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verification_request, parent, false)
        return VerificationRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerificationRequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = requests.size

    inner class VerificationRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val therapistName: TextView = itemView.findViewById(R.id.therapist_name)
        private val requestDate: TextView = itemView.findViewById(R.id.request_date)
        private val statusBadge: View = itemView.findViewById(R.id.status_badge)

        fun bind(request: VerificationRequest) {
            therapistName.text = request.therapistName

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = Date(request.timestamp)
            requestDate.text = dateFormat.format(date)

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(request)
            }
        }
    }
}
