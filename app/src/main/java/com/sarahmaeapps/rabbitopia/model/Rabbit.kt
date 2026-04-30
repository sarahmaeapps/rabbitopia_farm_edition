package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class Rabbit(
    @DocumentId val id: String = "",
    val earTattoo: String = "",
    val name: String = "",
    val breed: String = "",
    val sex: String = "", // "Buck" or "Doe"
    val color: String = "",
    val weight: Double = 0.0,
    val genCount: Int = 0,
    val sireId: String? = null,
    val damId: String? = null,
    val fatherId: String? = null, // Sync with Companion App
    val motherId: String? = null, // Sync with Companion App
    val status: String = "Breeder", // "Breeder", "Show", "4H Starter", "Meat", "Cull", "Sold"
    val hutchId: String = "",
    val imagePath: String? = null,
    val dateOfBirth: Long? = null,
    val dateOfCull: Long? = null,
    val cullReason: String? = null,
    val forSale: Boolean = false,
    val salePrice: Double = 0.0
)
