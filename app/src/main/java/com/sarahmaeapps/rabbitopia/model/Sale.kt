package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class Sale(
    @DocumentId val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val amount: Double = 0.0,
    val customerId: String = "",
    val rabbitId: String = "",
    val documentImageUrl: String? = null
)
