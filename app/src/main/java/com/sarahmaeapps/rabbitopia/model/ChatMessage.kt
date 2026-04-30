package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "", // Sync with companion app
    val receiverId: String = "",
    val text: String = "",
    val message: String = "", // Duplicate for Companion App compatibility
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false // Changed from isRead to match database
)
