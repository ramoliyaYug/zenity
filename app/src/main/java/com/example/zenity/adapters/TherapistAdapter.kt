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

    inner class TherapistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.therapist_name)
        private val specializationTextView: TextView = itemView.findViewById(R.id.therapist_specialization)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.therapist_description)

        fun bind(therapist: User) {
            nameTextView.text = therapist.name
            specializationTextView.text = therapist.specialization
            descriptionTextView.text = therapist.description

            itemView.setOnClickListener {
                onTherapistClick(therapist)
            }
        }
    }
}