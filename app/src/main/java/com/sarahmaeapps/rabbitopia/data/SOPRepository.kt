package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.SOPRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SOPRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val sopCollection = firestore.collection("sop_evaluations")

    fun getEvaluationsForRabbit(rabbitId: String): Flow<List<SOPRecord>> {
        return sopCollection
            .whereEqualTo("rabbitId", rabbitId)
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(SOPRecord::class.java) }
    }

    suspend fun addEvaluation(record: SOPRecord) {
        sopCollection.add(record).await()
    }

    suspend fun deleteEvaluation(id: String) {
        sopCollection.document(id).delete().await()
    }
}
