package com.sarahmaeapps.rabbitopia.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CustomerRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val customersCollection = firestore.collection("customers")

    fun getAllCustomers(): Flow<List<Customer>> {
        return customersCollection.snapshots().map { it.toObjects(Customer::class.java) }
    }

    suspend fun addCustomer(customer: Customer) {
        val lowercaseEmail = customer.email.trim().lowercase()
        if (lowercaseEmail.isNotEmpty()) {
            customersCollection.document(lowercaseEmail).set(customer.copy(email = lowercaseEmail)).await()
        }
    }

    suspend fun getCustomerById(id: String): Customer? {
        val lowercaseId = id.trim().lowercase()
        return customersCollection.document(lowercaseId).get().await().toObject(Customer::class.java)
    }

    suspend fun deleteCustomer(id: String) {
        val lowercaseId = id.trim().lowercase()
        customersCollection.document(lowercaseId).delete().await()
    }
}
