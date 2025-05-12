package com.example.zenity.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenity.R
import com.example.zenity.models.Session
import com.example.zenity.utils.FirebaseManager
import com.example.zenity.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : AppCompatActivity() {

    private lateinit var therapistNameTextView: TextView
    private lateinit var dateButton: Button
    private lateinit var timeSlotSpinner: Spinner
    private lateinit var sessionTypeSpinner: Spinner
    private lateinit var bookButton: Button
    private lateinit var prefManager: PreferenceManager

    private var therapistId: String? = null
    private var therapistName: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        prefManager = PreferenceManager(this)

        therapistId = intent.getStringExtra("THERAPIST_ID")
        therapistName = intent.getStringExtra("THERAPIST_NAME")

        if (therapistId == null || therapistName == null) {
            Toast.makeText(this, "Invalid booking parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.title = "Book Session"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupSpinners()
        setupDatePicker()
        setupBookButton()
    }

    private fun initViews() {
        therapistNameTextView = findViewById(R.id.therapist_name)
        dateButton = findViewById(R.id.date_button)
        timeSlotSpinner = findViewById(R.id.time_slot_spinner)
        sessionTypeSpinner = findViewById(R.id.session_type_spinner)
        bookButton = findViewById(R.id.book_button)

        therapistNameTextView.text = "Therapist: $therapistName"
        updateDateButtonText()
    }

    private fun setupSpinners() {
        // Time slot spinner
        val timeSlots = arrayOf("Morning", "Afternoon", "Evening")
        val timeSlotAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        timeSlotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSlotSpinner.adapter = timeSlotAdapter

        // Session type spinner
        val sessionTypes = arrayOf("Initial Consultation", "Follow-up Session")
        val sessionTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sessionTypes)
        sessionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sessionTypeSpinner.adapter = sessionTypeAdapter
    }

    private fun setupDatePicker() {
        dateButton.setOnClickListener {
            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDateButtonText()
            }, year, month, day).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        dateButton.text = dateFormat.format(selectedDate.time)
    }

    private fun setupBookButton() {
        bookButton.setOnClickListener {
            val patientId = prefManager.getUserId()
            if (patientId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timeSlot = timeSlotSpinner.selectedItem.toString().lowercase()
            val sessionType = if (sessionTypeSpinner.selectedItemPosition == 0) "initial" else "follow-up"

            val session = Session(
                patientId = patientId,
                therapistId = therapistId!!,
                date = selectedDate.timeInMillis,
                timeSlot = timeSlot,
                sessionType = sessionType
            )

            FirebaseManager.bookSession(session) { success, message ->
                if (success) {
                    Toast.makeText(this, "Session booked successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, message ?: "Failed to book session", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}