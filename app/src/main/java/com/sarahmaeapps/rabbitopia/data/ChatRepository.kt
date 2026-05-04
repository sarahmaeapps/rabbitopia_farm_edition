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
        val lowercaseUserId = userId.trim().lowercase()
        return firestore.collectionGroup("chat")
            .whereEqualTo("receiverId", lowercaseUserId)
            .whereEqualTo("read", false)
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.toObjects(ChatMessage::class.java)
            }
    }

    fun getAllChatThreads(): Flow<List<String>> {
        // Discovery logic: Look for any message sent to the admin or from the admin
        val adminEmail = "rabbitopiafarm@gmail.com"
        return firestore.collectionGroup("chat")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val sender = doc.getString("senderId")
                    val receiver = doc.getString("receiverId")
                    if (sender == adminEmail) receiver else sender
                }.filter { it != null && it != adminEmail }.distinct()
            }
    }

    suspend fun markAsRead(customerEmail: String, messageId: String) {
        val lowercaseEmail = customerEmail.trim().lowercase()
        firestore.collection("messages").document(lowercaseEmail).collection("chat")
            .document(messageId).update("read", true).await()
    }
}
