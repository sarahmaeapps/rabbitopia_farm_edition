package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class Litter(
    @DocumentId val id: String = "",
    val litterId: String = "", // Auto-generated 4-digit ID
    val damId: String = "",
    val sireId: String = "",
    val damName: String = "",
    val dateKindled: Long = System.currentTimeMillis(),
    val estimatedDueDate: Long? = null,
    val kitCount: Int = 0,
    val bornAlive: Int = 0,
    val stillborn: Int = 0,
    val mothersCareScore: Int = 0, // 0-9
    val notes: String = ""
)
