package com.sarahmaeapps.rabbitopia.model

import com.google.firebase.firestore.DocumentId

data class SOPRecord(
    @DocumentId val id: String = "",
    val rabbitId: String = "",
    val date: Long = System.currentTimeMillis(),
    val bodyScore: Int = 0,
    val headEarScore: Int = 0,
    val furScore: Int = 0,
    val colorScore: Int = 0,
    val conditionScore: Int = 0,
    val totalScore: Int = 0,
    val recommendation: String = "",
    val checkedItems: List<Boolean> = emptyList()
)
