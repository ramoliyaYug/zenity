package com.example.zenity.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.zenity.models.*

// Singleton to manage all Firebase operations
object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    // Authentication methods
    fun registerUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, auth.currentUser?.uid)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, auth.currentUser?.uid)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // User profile methods
    fun saveUserProfile(user: User, onComplete: (Boolean, String?) -> Unit) {
        val userId = user.userId.ifEmpty { getCurrentUserId() ?: return onComplete(false, "User not logged in") }
        database.child("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) null else task.exception?.message)
            }
    }

    fun getUserProfile(userId: String, onComplete: (User?) -> Unit) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                onComplete(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getUserProfile:onCancelled", error.toException())
                onComplete(null)
            }
        })
    }

    // Therapist methods
    fun getAllTherapists(onComplete: (List<User>) -> Unit) {
        database.child("users").orderByChild("userType").equalTo("therapist")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val therapists = mutableListOf<User>()
                    for (therapistSnapshot in snapshot.children) {
                        therapistSnapshot.getValue(User::class.java)?.let { therapists.add(it) }
                    }
                    onComplete(therapists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getAllTherapists:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    // Session booking methods
    fun bookSession(session: Session, onComplete: (Boolean, String?) -> Unit) {
        val sessionId = database.child("sessions").push().key ?: return onComplete(false, "Failed to generate session ID")
        val updatedSession = session.copy(sessionId = sessionId)

        database.child("sessions").child(sessionId).setValue(updatedSession)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) sessionId else task.exception?.message)
            }
    }

    fun getUserSessions(userId: String, onComplete: (List<Session>) -> Unit) {
        database.child("sessions").orderByChild("patientId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sessions = mutableListOf<Session>()
                    for (sessionSnapshot in snapshot.children) {
                        sessionSnapshot.getValue(Session::class.java)?.let { sessions.add(it) }
                    }
                    onComplete(sessions)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getUserSessions:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    fun getTherapistSessions(therapistId: String, onComplete: (List<Session>) -> Unit) {
        database.child("sessions").orderByChild("therapistId").equalTo(therapistId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sessions = mutableListOf<Session>()
                    for (sessionSnapshot in snapshot.children) {
                        sessionSnapshot.getValue(Session::class.java)?.let { sessions.add(it) }
                    }
                    onComplete(sessions)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getTherapistSessions:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    fun getAllPatients(onComplete: (List<User>) -> Unit) {
        database.child("users").orderByChild("userType").equalTo("patient")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val patients = mutableListOf<User>()
                    for (patientSnapshot in snapshot.children) {
                        patientSnapshot.getValue(User::class.java)?.let { patients.add(it) }
                    }
                    onComplete(patients)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getAllPatients:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    // Chat methods
    fun sendMessage(message: Message, onComplete: (Boolean, String?) -> Unit) {
        val messageId = database.child("messages").push().key ?: return onComplete(false, "Failed to generate message ID")
        val updatedMessage = message.copy(messageId = messageId)

        database.child("messages").child(messageId).setValue(updatedMessage)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) messageId else task.exception?.message)
            }
    }

    fun getMessages(userId1: String, userId2: String, onMessagesUpdate: (List<Message>) -> Unit): ValueEventListener {
        val messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null &&
                        ((message.senderId == userId1 && message.receiverId == userId2) ||
                                (message.senderId == userId2 && message.receiverId == userId1))) {
                        messages.add(message)
                    }
                }
                messages.sortBy { it.timestamp }
                onMessagesUpdate(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getMessages:onCancelled", error.toException())
                onMessagesUpdate(emptyList())
            }
        }

        database.child("messages").addValueEventListener(messagesListener)
        return messagesListener
    }

    // Forum methods
    fun createForumThread(thread: ForumThread, onComplete: (Boolean, String?) -> Unit) {
        val threadId = database.child("threads").push().key ?: return onComplete(false, "Failed to generate thread ID")
        val updatedThread = thread.copy(threadId = threadId)

        database.child("threads").child(threadId).setValue(updatedThread)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) threadId else task.exception?.message)
            }
    }

    fun getAllThreads(onComplete: (List<ForumThread>) -> Unit) {
        database.child("threads").orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val threads = mutableListOf<ForumThread>()
                    for (threadSnapshot in snapshot.children) {
                        threadSnapshot.getValue(ForumThread::class.java)?.let { threads.add(it) }
                    }
                    threads.reverse() // Most recent first
                    onComplete(threads)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getAllThreads:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    fun addReplyToThread(reply: ThreadReply, onComplete: (Boolean, String?) -> Unit) {
        val replyId = database.child("replies").push().key ?: return onComplete(false, "Failed to generate reply ID")
        val updatedReply = reply.copy(replyId = replyId)

        database.child("replies").child(replyId).setValue(updatedReply)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) replyId else task.exception?.message)
            }
    }

    fun getThreadReplies(threadId: String, onComplete: (List<ThreadReply>) -> Unit) {
        database.child("replies").orderByChild("threadId").equalTo(threadId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val replies = mutableListOf<ThreadReply>()
                    for (replySnapshot in snapshot.children) {
                        replySnapshot.getValue(ThreadReply::class.java)?.let { replies.add(it) }
                    }
                    replies.sortBy { it.timestamp }
                    onComplete(replies)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getThreadReplies:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }
}