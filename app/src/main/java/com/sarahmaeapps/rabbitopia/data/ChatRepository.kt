package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getMessages(customerEmail: String): Flow<List<ChatMessage>> {
        val lowercaseEmail = customerEmail.trim().lowercase()
        return firestore.collection("messages").document(lowercaseEmail).collection("chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatMessage::class.java)
            }
    }

    suspend fun sendMessage(customerEmail: String, message: ChatMessage) {
        val lowercaseEmail = customerEmail.trim().lowercase()
        firestore.collection("messages").document(lowercaseEmail).collection("chat")
            .add(message).await()
    }

    fun getUnreadMessages(userId: String): Flow<List<ChatMessage>> {
        // This is a bit tricky now since messages are nested. 
        // We'll need a Collection Group Query if we want to find unread messages across all customers efficiently.
        // For now, let's just use the top-level structure for alerts if possible, or keep as is.
        // But the user requested messages/{email}/chat.
        val lowercaseUserId = userId.trim().lowercase()
        return firestore.collectionGroup("chat")
            .whereEqualTo("receiverId", lowercaseUserId)
            .whereEqualTo("isRead", false)
            .snapshots()
            .map { it.toObjects(ChatMessage::class.java) }
    }

    suspend fun markAsRead(customerEmail: String, messageId: String) {
        val lowercaseEmail = customerEmail.trim().lowercase()
        firestore.collection("messages").document(lowercaseEmail).collection("chat")
            .document(messageId).update("isRead", true).await()
    }
}
