package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.sarahmaeapps.rabbitopia.model.CullRecord
import kotlinx.coroutines.tasks.await

class CullRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cullsCollection = firestore.collection("culls")

    suspend fun addCullRecord(record: CullRecord) {
        firestore.runTransaction { transaction ->
            // Add cull record
            val newCullRef = cullsCollection.document()
            transaction.set(newCullRef, record)
            
            // Update rabbit status to Culled
            val rabbitRef = firestore.collection("rabbits").document(record.rabbitId)
            transaction.update(rabbitRef, "status", "Culled")
        }.await()
    }
}
