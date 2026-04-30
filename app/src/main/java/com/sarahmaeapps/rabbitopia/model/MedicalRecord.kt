package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class MedicalRecord(
    @DocumentId val id: String = "",
    val rabbitId: String = "",
    val date: Long = System.currentTimeMillis(),
    val condition: String = "",
    val treatment: String = "",
    val medications: String = "",
    val vetNotes: String = "",
    val isCullingIssue: Boolean = false,
    val imagePath: String? = null,
    val cost: Double = 0.0,
    val type: String = "Medical" // "Medical" or "Non-Medical"
)
