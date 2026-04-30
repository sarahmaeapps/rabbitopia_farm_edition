package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class FodderBatch(
    @DocumentId val id: String = "",
    val batchID: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val harvestDate: Long? = null,
    val yieldWeight: Double = 0.0,
    val temperature: Double = 0.0,
    val soakTimeHours: Int = 0,
    val notes: String = "",
    val cost: Double = 0.0
)
