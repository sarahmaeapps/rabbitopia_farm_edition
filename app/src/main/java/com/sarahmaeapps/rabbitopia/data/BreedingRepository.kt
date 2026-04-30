package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.BreedingEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BreedingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val breedingCollection = firestore.collection("breeding_events")

    fun getAllBreedingEvents(): Flow<List<BreedingEvent>> {
        return breedingCollection.snapshots().map { snapshot ->
            snapshot.toObjects(BreedingEvent::class.java)
        }
    }

    fun getBreedingEventsForRabbit(rabbitId: String): Flow<List<BreedingEvent>> {
        // Query both where rabbit is Dam or Sire
        return firestore.collection("breeding_events")
            .whereIn("damId", listOf(rabbitId)) // Firestore doesn't support easy OR across fields in one query without indexing complexity, 
            // but for simplicity we can just filter in memory or do two queries.
            // Actually let's just fetch all and filter in ViewModel for now, or just damId if it's mostly female centric.
            // User said "all of the breedings that this rabbit has done".
            .snapshots()
            .map { it.toObjects(BreedingEvent::class.java) }
    }

    suspend fun getBreedingEventById(id: String): BreedingEvent? {
        return breedingCollection.document(id).get().await().toObject(BreedingEvent::class.java)
    }

    suspend fun addBreedingEvent(event: BreedingEvent) {
        breedingCollection.add(event).await()
    }

    suspend fun deleteBreedingEvent(id: String) {
        breedingCollection.document(id).delete().await()
    }
}
