package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class Customer(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val arbaNumber: String = "",
    val lifetimeValue: Double = 0.0,
    val notes: String = ""
)
