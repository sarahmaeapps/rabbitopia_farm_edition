package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class CullRecord(
    @DocumentId val id: String = "",
    val rabbitId: String = "",
    val date: Long = System.currentTimeMillis(),
    val reason: String = "",
    val processedBy: String = "",
    val processedFor: String = ""
)
