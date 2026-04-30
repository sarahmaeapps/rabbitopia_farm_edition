package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class WeighInRecord(
    @DocumentId val id: String = "",
    val rabbitId: String = "",
    val date: Long = System.currentTimeMillis(),
    val weight: Double = 0.0,
    val notes: String = ""
)
