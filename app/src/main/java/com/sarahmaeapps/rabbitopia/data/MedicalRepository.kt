package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.MedicalRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MedicalRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val medicalCollection = firestore.collection("medical")

    fun getAllRecords(): Flow<List<MedicalRecord>> {
        return medicalCollection.snapshots().map { it.toObjects(MedicalRecord::class.java) }
    }

    suspend fun addRecord(record: MedicalRecord) {
        medicalCollection.add(record).await()
    }

    suspend fun getRecordById(id: String): MedicalRecord? {
        return medicalCollection.document(id).get().await().toObject(MedicalRecord::class.java)
    }

    suspend fun updateRecord(record: MedicalRecord) {
        if (record.id.isNotEmpty()) {
            medicalCollection.document(record.id).set(record).await()
        }
    }
    
    fun getRecordsForRabbit(rabbitId: String): Flow<List<MedicalRecord>> {
        return medicalCollection
            .whereEqualTo("rabbitId", rabbitId)
            .snapshots()
            .map { it.toObjects(MedicalRecord::class.java) }
    }

    suspend fun deleteRecord(id: String) {
        medicalCollection.document(id).delete().await()
    }
}
