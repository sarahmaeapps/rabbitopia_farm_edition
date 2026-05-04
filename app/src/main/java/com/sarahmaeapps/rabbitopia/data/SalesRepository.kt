package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.sarahmaeapps.rabbitopia.model.Sale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import android.net.Uri

class SalesRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val salesCollection = firestore.collection("sales")
    private val listingsCollection = firestore.collection("listings")

    fun getAllSales(): Flow<List<Sale>> {
        return salesCollection.snapshots().map { it.toObjects(Sale::class.java) }
    }

    fun getAllListings(): Flow<List<com.sarahmaeapps.rabbitopia.model.Listing>> {
        return listingsCollection.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots().map { it.toObjects(com.sarahmaeapps.rabbitopia.model.Listing::class.java) }
    }

    suspend fun addListing(listing: com.sarahmaeapps.rabbitopia.model.Listing, imageUri: Uri? = null) {
        var finalListing = listing
        if (imageUri != null) {
            try {
                val ref = storage.reference.child("listings/${System.currentTimeMillis()}.jpg")
                ref.putFile(imageUri).await()
                val url = ref.downloadUrl.await().toString()
                finalListing = listing.copy(imagePath = url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        if (finalListing.id.isEmpty()) {
            listingsCollection.add(finalListing).await()
        } else {
            listingsCollection.document(finalListing.id).set(finalListing).await()
        }
    }

    suspend fun deleteListing(id: String) {
        listingsCollection.document(id).delete().await()
    }

    suspend fun addSale(sale: Sale, documentUri: Uri? = null) {
        var finalSale = sale
        if (documentUri != null) {
            val saleId = if (sale.id.isEmpty()) firestore.collection("sales").document().id else sale.id
            var url: String? = null
            try {
                url = uploadDocumentImage(documentUri, saleId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finalSale = sale.copy(id = saleId, documentImageUrl = url)
        }

        firestore.runTransaction { transaction ->
            // 1. All READS must happen first
            val customerRef = if (finalSale.customerId != "SYSTEM_DOC") {
                firestore.collection("customers").document(finalSale.customerId)
            } else null
            
            val currentLTV = customerRef?.let { transaction.get(it).getDouble("lifetimeValue") } ?: 0.0
            
            // 2. All WRITES happen after
            // Save to top-level for Admin view
            val globalSaleRef = if (finalSale.id.isEmpty()) salesCollection.document() else salesCollection.document(finalSale.id)
            transaction.set(globalSaleRef, finalSale)

            // Save to Customer sub-collection for Companion app (if not just a system doc)
            if (finalSale.customerId != "SYSTEM_DOC" && finalSale.rabbitId != "SYSTEM_DOC") {
                val customerSaleRef = firestore.collection("customers").document(finalSale.customerId).collection("sales").document(globalSaleRef.id)
                transaction.set(customerSaleRef, finalSale.copy(id = globalSaleRef.id))
                
                // Update customer lifetime value
                customerRef?.let { transaction.update(it, "lifetimeValue", currentLTV + finalSale.amount) }

                // Update rabbit status to Sold
                val rabbitRef = firestore.collection("rabbits").document(finalSale.rabbitId)
                transaction.update(rabbitRef, "status", "Sold")

                // Record in purchasedRabbits sub-collection for Companion app
                val purchasedRabbitRef = firestore.collection("customers").document(finalSale.customerId).collection("purchasedRabbits").document(finalSale.rabbitId)
                transaction.set(purchasedRabbitRef, mapOf("rabbitId" to finalSale.rabbitId, "saleId" to globalSaleRef.id, "purchaseDate" to finalSale.date))
            }
        }.await()
    }

    suspend fun getSaleById(id: String): Sale? {
        return salesCollection.document(id).get().await().toObject(Sale::class.java)
    }

    suspend fun uploadDocumentImage(uri: Uri, saleId: String): String {
        val ref = storage.reference.child("sales_docs/$saleId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteSale(id: String) {
        // This is complex because we should ideally reverse the LTV and Rabbit status, 
        // but for now let's just delete the record to clean up.
        salesCollection.document(id).delete().await()
        // Also delete from customer subcollection if we had the customer ID... 
        // For simple cleanup, we'll just delete the main one.
    }
}
