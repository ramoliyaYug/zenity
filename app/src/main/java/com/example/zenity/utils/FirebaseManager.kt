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

    // Enhanced verifyTherapist method with additional logging and verification
    fun verifyTherapist(therapistId: String, isVerified: Boolean, notes: String, onComplete: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Verifying therapist $therapistId, isVerified=$isVerified")

        // First, check if the user exists and is a therapist
        database.child("users").child(therapistId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null && user.userType == "therapist") {
                    Log.d(TAG, "Found therapist: ${user.name}, current verification: ${user.isVerified}")

                    // Create updated user with verification status
                    val updatedUser = user.copy(isVerified = isVerified, verificationNotes = notes)

                    // Update the user in Firebase with explicit isVerified field
                    val updates = HashMap<String, Any>()
                    updates["isVerified"] = isVerified
                    updates["verificationNotes"] = notes

                    database.child("users").child(therapistId).updateChildren(updates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Therapist verification updated successfully to: $isVerified")

                                // Double-check the update was successful by reading back the data
                                database.child("users").child(therapistId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(verifySnapshot: DataSnapshot) {
                                        val verifiedUser = verifySnapshot.getValue(User::class.java)
                                        Log.d(TAG, "Verification status after update: ${verifiedUser?.isVerified}")
                                        onComplete(true, "Therapist verification updated successfully")
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(TAG, "Verification check cancelled", error.toException())
                                        onComplete(true, "Therapist verification updated but unable to confirm")
                                    }
                                })
                            } else {
                                Log.e(TAG, "Failed to update therapist verification", task.exception)
                                onComplete(false, task.exception?.message)
                            }
                        }
                } else {
                    Log.e(TAG, "User not found or not a therapist")
                    onComplete(false, "User not found or not a therapist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "verifyTherapist:onCancelled", error.toException())
                onComplete(false, error.message)
            }
        })
    }

    // Enhanced getVerifiedTherapists method with improved filtering
    fun getVerifiedTherapists(onComplete: (List<User>) -> Unit) {
        Log.d(TAG, "Getting verified therapists with improved method")

        // Use a compound query to get only verified therapists
        // First get all therapists
        database.child("users").orderByChild("userType").equalTo("therapist")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val therapists = mutableListOf<User>()
                    Log.d(TAG, "Found ${snapshot.childrenCount} therapists total")

                    // Manually filter for verified therapists with explicit logging
                    for (therapistSnapshot in snapshot.children) {
                        // Get the raw data to check if isVerified exists and is true
                        val rawData = therapistSnapshot.value as? Map<*, *>
                        val isVerifiedRaw = rawData?.get("isVerified")

                        // Get the parsed User object
                        val therapist = therapistSnapshot.getValue(User::class.java)

                        Log.d(TAG, "Therapist: ${therapist?.name}, userId: ${therapist?.userId}")
                        Log.d(TAG, "Raw isVerified value: $isVerifiedRaw, Parsed isVerified: ${therapist?.isVerified}")

                        // Only add therapists that are explicitly verified
                        if (therapist != null && (isVerifiedRaw == true || therapist.isVerified)) {
                            Log.d(TAG, "Adding verified therapist: ${therapist.name}")

                            // Ensure the isVerified flag is set correctly in the object we're returning
                            val verifiedTherapist = if (!therapist.isVerified) {
                                therapist.copy(isVerified = true)
                            } else {
                                therapist
                            }

                            therapists.add(verifiedTherapist)
                        }
                    }

                    Log.d(TAG, "Returning ${therapists.size} verified therapists")
                    onComplete(therapists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getVerifiedTherapists:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    // Get all unverified therapists
    fun getUnverifiedTherapists(onComplete: (List<User>) -> Unit) {
        database.child("users").orderByChild("userType").equalTo("therapist")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val therapists = mutableListOf<User>()
                    for (therapistSnapshot in snapshot.children) {
                        val therapist = therapistSnapshot.getValue(User::class.java)
                        if (therapist != null && !therapist.isVerified) {
                            therapists.add(therapist)
                        }
                    }
                    onComplete(therapists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getUnverifiedTherapists:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    fun updateVerificationRequest(requestId: String, status: String, adminNotes: String, onComplete: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Updating verification request: $requestId, status: $status")
        database.child("verificationRequests").child(requestId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val request = snapshot.getValue(VerificationRequest::class.java)
                    if (request != null) {
                        Log.d(TAG, "Found request for therapist: ${request.therapistName}, therapistId: ${request.therapistId}")
                        val updatedRequest = request.copy(status = status, adminNotes = adminNotes)

                        // Update the request first
                        database.child("verificationRequests").child(requestId).setValue(updatedRequest)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Request updated successfully, now updating therapist verification")

                                    if (status == "approved") {
                                        // If approved, update the therapist's verification status directly using updateChildren
                                        val updates = HashMap<String, Any>()
                                        updates["isVerified"] = true
                                        updates["verificationNotes"] = adminNotes

                                        database.child("users").child(request.therapistId).updateChildren(updates)
                                            .addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    Log.d(TAG, "Therapist verification updated successfully to TRUE")

                                                    // Double-check the update was successful
                                                    database.child("users").child(request.therapistId)
                                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(verifySnapshot: DataSnapshot) {
                                                                val verifiedUser = verifySnapshot.getValue(User::class.java)
                                                                Log.d(TAG, "Verification status after approval: ${verifiedUser?.isVerified}")
                                                                onComplete(true, "Therapist verified successfully")
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                Log.e(TAG, "Verification check cancelled", error.toException())
                                                                onComplete(true, "Therapist verification updated but unable to confirm")
                                                            }
                                                        })
                                                } else {
                                                    Log.e(TAG, "Failed to update therapist verification", verifyTask.exception)
                                                    onComplete(false, verifyTask.exception?.message)
                                                }
                                            }
                                    } else if (status == "rejected") {
                                        // If rejected, ensure therapist is not verified using updateChildren
                                        val updates = HashMap<String, Any>()
                                        updates["isVerified"] = false
                                        updates["verificationNotes"] = adminNotes

                                        database.child("users").child(request.therapistId).updateChildren(updates)
                                            .addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    Log.d(TAG, "Therapist verification updated successfully to FALSE")
                                                    onComplete(true, "Therapist verification rejected")
                                                } else {
                                                    Log.e(TAG, "Failed to update therapist verification", verifyTask.exception)
                                                    onComplete(false, verifyTask.exception?.message)
                                                }
                                            }
                                    } else {
                                        // For other statuses, just return success
                                        onComplete(true, "Request updated successfully")
                                    }
                                } else {
                                    Log.e(TAG, "Failed to update request", task.exception)
                                    onComplete(false, task.exception?.message)
                                }
                            }
                    } else {
                        Log.e(TAG, "Verification request not found")
                        onComplete(false, "Verification request not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "updateVerificationRequest:onCancelled", error.toException())
                    onComplete(false, error.message)
                }
            })
    }

    // Verification request methods
    fun submitVerificationRequest(therapistId: String, credentials: String, onComplete: (Boolean, String?) -> Unit) {
        getUserProfile(therapistId) { user ->
            if (user != null && user.userType == "therapist") {
                val requestId = database.child("verificationRequests").push().key ?:
                return@getUserProfile onComplete(false, "Failed to generate request ID")

                val request = VerificationRequest(
                    requestId = requestId,
                    therapistId = therapistId,
                    therapistName = user.name,
                    timestamp = System.currentTimeMillis(),
                    credentials = credentials
                )

                database.child("verificationRequests").child(requestId).setValue(request)
                    .addOnCompleteListener { task ->
                        onComplete(task.isSuccessful, if (task.isSuccessful) requestId else task.exception?.message)
                    }
            } else {
                onComplete(false, "User not found or not a therapist")
            }
        }
    }

    fun getAllVerificationRequests(onComplete: (List<VerificationRequest>) -> Unit) {
        database.child("verificationRequests")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requests = mutableListOf<VerificationRequest>()
                    for (requestSnapshot in snapshot.children) {
                        requestSnapshot.getValue(VerificationRequest::class.java)?.let { requests.add(it) }
                    }
                    requests.sortByDescending { it.timestamp }
                    onComplete(requests)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getAllVerificationRequests:onCancelled", error.toException())
                    onComplete(emptyList())
                }
            })
    }

    fun getPendingVerificationRequests(onComplete: (List<VerificationRequest>) -> Unit) {
        database.child("verificationRequests").orderByChild("status").equalTo("pending")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requests = mutableListOf<VerificationRequest>()
                    for (requestSnapshot in snapshot.children) {
                        requestSnapshot.getValue(VerificationRequest::class.java)?.let { requests.add(it) }
                    }
                    requests.sortByDescending { it.timestamp }
                    onComplete(requests)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getPendingVerificationRequests:onCancelled", error.toException())
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

    // Add this method at the end of the FirebaseManager object to create an admin account
    fun createAdminAccount(email: String, password: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        // First check if any admin accounts already exist
        database.child("users").orderByChild("userType").equalTo("admin")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        // Admin account already exists
                        onComplete(false, "Admin account already exists. Contact existing admin for access.")
                        return
                    }

                    // No admin exists, create one
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    val adminUser = User(
                                        userId = userId,
                                        email = email,
                                        name = name,
                                        userType = "admin"
                                    )

                                    database.child("users").child(userId).setValue(adminUser)
                                        .addOnCompleteListener { profileTask ->
                                            onComplete(profileTask.isSuccessful,
                                                if (profileTask.isSuccessful) "Admin account created successfully"
                                                else profileTask.exception?.message)
                                        }
                                } else {
                                    onComplete(false, "Failed to get user ID")
                                }
                            } else {
                                onComplete(false, task.exception?.message)
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false, error.message)
                }
            })
    }
}
