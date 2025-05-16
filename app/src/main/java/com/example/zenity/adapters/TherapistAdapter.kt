package com.example.zenity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zenity.R
import com.example.zenity.models.User

class TherapistAdapter(private val onTherapistClick: (User) -> Unit) :
    RecyclerView.Adapter<TherapistAdapter.TherapistViewHolder>() {

    private val therapists = mutableListOf<User>()

    fun setTherapists(newTherapists: List<User>) {
        therapists.clear()
        therapists.addAll(newTherapists)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TherapistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_therapist, parent, false)
        return TherapistViewHolder(view)
    }

    override fun onBindViewHolder(holder: TherapistViewHolder, position: Int) {
        holder.bind(therapists[position])
    }

    override fun getItemCount(): Int = therapists.size

    // Update the TherapistViewHolder class to show verification status
    inner class TherapistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.therapist_name)
        private val specializationTextView: TextView = itemView.findViewById(R.id.therapist_specialization)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.therapist_description)
        private val verifiedIcon: View = itemView.findViewById(R.id.verified_icon)

        fun bind(therapist: User) {
            // Log therapist data for debugging
            android.util.Log.d("TherapistAdapter", "Binding therapist: ${therapist.name}, isVerified: ${therapist.isVerified}")

            nameTextView.text = therapist.name

            // Update specialization text to indicate verification status
            if (therapist.isVerified) {
                specializationTextView.text = "${therapist.specialization ?: "General Therapy"} • Verified"
                specializationTextView.setTextColor(itemView.context.getColor(R.color.colorSuccess))
            } else {
                specializationTextView.text = therapist.specialization ?: "General Therapy"
                specializationTextView.setTextColor(itemView.context.getColor(R.color.colorPrimary))
            }

            descriptionTextView.text = therapist.description

            // Show verification icon only if therapist is verified
            verifiedIcon.visibility = if (therapist.isVerified) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onTherapistClick(therapist)
            }
        }
    }
}
