package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.ConditionerLog
import com.sarahmaeapps.rabbitopia.model.FeedPurchase
import com.sarahmaeapps.rabbitopia.model.FodderBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FodderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val fodderCollection = firestore.collection("fodder")
    private val feedCollection = firestore.collection("feed_purchases")
    private val conditionerCollection = firestore.collection("conditioners")

    fun getAllBatches(): Flow<List<FodderBatch>> {
        return fodderCollection
            .orderBy("startDate")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FodderBatch::class.java)
            }
    }

    fun getAllFeedPurchases(): Flow<List<FeedPurchase>> {
        return feedCollection
            .orderBy("purchaseDate")
            .snapshots()
            .map { it.toObjects(FeedPurchase::class.java) }
    }

    fun getAllConditioners(): Flow<List<ConditionerLog>> {
        return conditionerCollection
            .orderBy("date")
            .snapshots()
            .map { it.toObjects(ConditionerLog::class.java) }
    }

    suspend fun addBatch(batch: FodderBatch) {
        fodderCollection.add(batch).await()
    }

    suspend fun addFeedPurchase(purchase: FeedPurchase) {
        feedCollection.add(purchase).await()
    }

    suspend fun addConditioner(log: ConditionerLog) {
        conditionerCollection.add(log).await()
    }

    suspend fun updateFeedPurchase(purchase: FeedPurchase) {
        if (purchase.id.isNotEmpty()) {
            feedCollection.document(purchase.id).set(purchase).await()
        }
    }

    suspend fun updateConditioner(log: ConditionerLog) {
        if (log.id.isNotEmpty()) {
            conditionerCollection.document(log.id).set(log).await()
        }
    }

    suspend fun updateBatch(batch: FodderBatch) {
        if (batch.id.isNotEmpty()) {
            fodderCollection.document(batch.id).set(batch).await()
        }
    }

    suspend fun deleteBatch(id: String) {
        fodderCollection.document(id).delete().await()
    }

    suspend fun deleteFeedPurchase(id: String) {
        feedCollection.document(id).delete().await()
    }

    suspend fun deleteConditioner(id: String) {
        conditionerCollection.document(id).delete().await()
    }
}
