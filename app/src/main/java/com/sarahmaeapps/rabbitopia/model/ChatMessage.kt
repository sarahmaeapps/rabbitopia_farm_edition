package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class ChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "", // Sync with companion app
    val receiverId: String = "",
    val text: String = "",
    val message: String = "", // Duplicate for Companion App compatibility
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestampRaw: Any? = null,
    val read: Boolean = false
) {
    @get:Exclude
    val timestamp: Long
        get() = when (val t = timestampRaw) {
            is Long -> t
            is Timestamp -> t.toDate().time
            else -> System.currentTimeMillis()
        }
}
