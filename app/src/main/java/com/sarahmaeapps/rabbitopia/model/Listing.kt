package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class Listing(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
