package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.WeighInRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WeighInRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val weighInCollection = firestore.collection("weigh_ins")

    fun getWeighInsForRabbit(rabbitId: String): Flow<List<WeighInRecord>> {
        return weighInCollection
            .whereEqualTo("rabbitId", rabbitId)
            .snapshots()
            .map { it.toObjects(WeighInRecord::class.java) }
    }

    suspend fun addWeighIn(record: WeighInRecord) {
        weighInCollection.add(record).await()
    }
}
