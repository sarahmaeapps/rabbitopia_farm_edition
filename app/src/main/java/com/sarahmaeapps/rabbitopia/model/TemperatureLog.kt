package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class TemperatureLog(
    @DocumentId val id: String = "",
    val hutchId: String = "",
    val temperature: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)
