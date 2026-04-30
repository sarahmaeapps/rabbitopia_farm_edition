package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class FeedPurchase(
    @DocumentId val id: String = "",
    val brand: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val price: Double = 0.0,
    val weightLbs: Double = 0.0,
    val supplier: String = "",
    val proteinPercent: Double = 0.0,
    val fiberPercent: Double = 0.0,
    val fatPercent: Double = 0.0,
    val notes: String = ""
)
