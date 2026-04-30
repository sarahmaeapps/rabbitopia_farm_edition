package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class ConditionerLog(
    @DocumentId val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: String = "", // BOSS, Oats, Papaya, etc.
    val notes: String = "",
    val cost: Double = 0.0
)
