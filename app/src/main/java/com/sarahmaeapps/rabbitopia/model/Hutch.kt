package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class MaintenanceRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val cost: Double = 0.0
)

data class Hutch(
    @DocumentId val id: String = "",
    val hutchId: String = "",
    val status: String = "Good", // "Maintenance Required", "Damaged"
    val maintenanceNotes: String = "",
    val upgrades: String = "",
    val predatorySignsCheck: Boolean = false,
    val predatoryNotes: String = "",
    val capacity: Int = 1,
    val maintenanceRecords: List<MaintenanceRecord> = emptyList()
)
