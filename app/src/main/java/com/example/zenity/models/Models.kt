package com.example.zenity.models

// User model for both patients and therapists
data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val userType: String = "", // "patient" or "therapist"
    val age: Int? = null, // for patients
    val description: String = "",
    val specialization: String = "" // for therapists
)

// Session booking model
data class Session(
    val sessionId: String = "",
    val patientId: String = "",
    val therapistId: String = "",
    val date: Long = 0, // timestamp
    val timeSlot: String = "", // "morning", "afternoon", "evening"
    val sessionType: String = "" // "initial", "follow-up"
)

// Chat message model
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0
)

// Forum thread model
data class ForumThread(
    val threadId: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = 0
)

// Thread reply model
data class ThreadReply(
    val replyId: String = "",
    val threadId: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = 0
)