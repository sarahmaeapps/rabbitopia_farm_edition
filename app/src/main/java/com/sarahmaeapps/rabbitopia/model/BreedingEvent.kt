package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class BreedingEvent(
    @DocumentId val id: String = "",
    val sireId: String = "",
    val damId: String = "",
    val breedingDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (31L * 24 * 60 * 60 * 1000), // +31 days
    val status: String = "Expected", // "Expected", "Kindled", "Failed"
    val notes: String = ""
)
