package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.Litter
import com.sarahmaeapps.rabbitopia.model.Rabbit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LitterRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val littersCollection = firestore.collection("litters")
    private val rabbitsCollection = firestore.collection("rabbits")

    fun getLittersForRabbit(rabbitId: String): Flow<List<Litter>> {
        return littersCollection
            .whereEqualTo("damId", rabbitId)
            .snapshots()
            .map { it.toObjects(Litter::class.java) }
    }

    suspend fun addLitter(litter: Litter, kits: List<Rabbit>) {
        firestore.runTransaction { transaction ->
            // 1. Save Litter Record
            val newLitterRef = littersCollection.document()
            transaction.set(newLitterRef, litter.copy(id = newLitterRef.id))

            // 2. Save Kit Records
            kits.forEach { kit ->
                val newKitRef = rabbitsCollection.document()
                transaction.set(newKitRef, kit.copy(id = newKitRef.id))
            }
        }.await()
    }

    suspend fun getLitterById(id: String): Litter? {
        return littersCollection.document(id).get().await().toObject(Litter::class.java)
    }

    suspend fun deleteLitter(id: String) {
        littersCollection.document(id).delete().await()
    }
}
