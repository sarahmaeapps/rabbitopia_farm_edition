package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
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
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SOPRecord::class.java)
            }
    }

    suspend fun addEvaluation(record: SOPRecord) {
        try {
            android.util.Log.d("SOPRepository", "Attempting to add SOP evaluation for rabbit: ${record.rabbitId}")
            val docRef = sopCollection.add(record).await()
            android.util.Log.d("SOPRepository", "SOP evaluation added successfully with ID: ${docRef.id}")
        } catch (e: Exception) {
            android.util.Log.e("SOPRepository", "Error adding SOP evaluation: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteEvaluation(id: String) {
        sopCollection.document(id).delete().await()
    }
}
