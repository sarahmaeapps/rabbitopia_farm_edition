package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.sarahmaeapps.rabbitopia.model.Rabbit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import android.net.Uri
import kotlinx.coroutines.CancellationException

class RabbitRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val rabbitsCollection = firestore.collection("rabbits")

    fun getActiveRabbits(): Flow<List<Rabbit>> {
        return rabbitsCollection
            .whereIn("status", listOf("Breeder", "Show", "4H Starter", "Meat"))
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Rabbit::class.java)
            }
    }

    fun getCulledRabbits(): Flow<List<Rabbit>> {
        return rabbitsCollection
            .whereEqualTo("status", "Cull")
            .snapshots()
            .map { it.toObjects(Rabbit::class.java) }
    }

    fun getBreederBucks(): Flow<List<Rabbit>> {
        return rabbitsCollection
            .whereEqualTo("status", "Breeder")
            .whereEqualTo("sex", "Buck")
            .snapshots()
            .map { it.toObjects(Rabbit::class.java) }
    }

    fun getBreederDoes(): Flow<List<Rabbit>> {
        return rabbitsCollection
            .whereEqualTo("status", "Breeder")
            .whereEqualTo("sex", "Doe")
            .snapshots()
            .map { it.toObjects(Rabbit::class.java) }
    }

    suspend fun addRabbit(rabbit: Rabbit) {
        val docRef = rabbitsCollection.document()
        val rabbitWithId = rabbit.copy(id = docRef.id)
        docRef.set(rabbitWithId).await()
        if (rabbitWithId.forSale) {
            firestore.collection("forsale").document(docRef.id).set(rabbitWithId).await()
        }
    }

    suspend fun addRabbitWithImage(rabbit: Rabbit, imageUri: Uri): String {
        val docRef = rabbitsCollection.document()
        val rabbitId = docRef.id
        
        try {
            // 1. Upload Image First
            android.util.Log.d("RabbitRepository", "Starting image upload for rabbit $rabbitId")
            val imageUrl = uploadRabbitImage(imageUri, rabbitId)
            android.util.Log.d("RabbitRepository", "Image uploaded successfully: $imageUrl")
            
            // 2. Save with Image URL
            val rabbitWithId = rabbit.copy(id = rabbitId, imagePath = imageUrl)
            docRef.set(rabbitWithId).await()
            android.util.Log.d("RabbitRepository", "Rabbit document saved with image path")
            
            if (rabbitWithId.forSale) {
                firestore.collection("forsale").document(rabbitId).set(rabbitWithId).await()
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                android.util.Log.w("RabbitRepository", "Add rabbit cancelled")
                throw e
            }
            android.util.Log.e("RabbitRepository", "Failed to add rabbit with image: ${e.message}", e)
            // Fallback: save without image if upload failed
            val rabbitWithId = rabbit.copy(id = rabbitId)
            docRef.set(rabbitWithId).await()
            if (rabbitWithId.forSale) {
                firestore.collection("forsale").document(rabbitId).set(rabbitWithId).await()
            }
        }
        return rabbitId
    }

    suspend fun getRabbitById(id: String): Rabbit? {
        return rabbitsCollection.document(id).get().await().toObject(Rabbit::class.java)
    }

    fun getRabbitFlow(id: String): Flow<Rabbit?> {
        return rabbitsCollection.document(id).snapshots().map { it.toObject(Rabbit::class.java) }
    }
    
    suspend fun updateRabbit(rabbit: Rabbit, newImageUri: Uri? = null) {
        if (rabbit.id.isNotEmpty()) {
            var updatedRabbit = rabbit
            
            if (newImageUri != null) {
                try {
                    android.util.Log.d("RabbitRepository", "Updating image for rabbit ${rabbit.id}")
                    val imageUrl = uploadRabbitImage(newImageUri, rabbit.id)
                    updatedRabbit = rabbit.copy(imagePath = imageUrl)
                    android.util.Log.d("RabbitRepository", "Image updated: $imageUrl")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    android.util.Log.e("RabbitRepository", "Failed to upload new image during update", e)
                }
            }
            
            // Save the updated rabbit object (preserves existing imagePath if newImageUri was null)
            rabbitsCollection.document(updatedRabbit.id).set(updatedRabbit).await()
            
            if (updatedRabbit.forSale) {
                firestore.collection("forsale").document(updatedRabbit.id).set(updatedRabbit).await()
            } else {
                firestore.collection("forsale").document(updatedRabbit.id).delete().await()
            }
        }
    }

    suspend fun uploadRabbitImage(uri: Uri, rabbitId: String): String {
        val ref = storage.reference.child("rabbits/$rabbitId.jpg")
        android.util.Log.d("RabbitRepository", "Uploading to: ${ref.path}")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        android.util.Log.d("RabbitRepository", "Upload complete. URL: $url")
        return url
    }

    suspend fun deleteRabbit(id: String) {
        rabbitsCollection.document(id).delete().await()
        firestore.collection("forsale").document(id).delete().await()
        // Optional: delete image from storage if it exists
        try {
            storage.reference.child("rabbits/$id.jpg").delete().await()
        } catch (e: Exception) {
            // Might not have an image
        }
    }
}
