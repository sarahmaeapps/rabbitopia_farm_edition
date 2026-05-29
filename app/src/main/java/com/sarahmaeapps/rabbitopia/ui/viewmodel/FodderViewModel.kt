package com.sarahmaeapps.rabbitopia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahmaeapps.rabbitopia.data.FodderRepository
import com.sarahmaeapps.rabbitopia.model.ConditionerLog
import com.sarahmaeapps.rabbitopia.model.FeedPurchase
import com.sarahmaeapps.rabbitopia.model.FodderBatch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FodderViewModel(private val repository: FodderRepository = FodderRepository()) : ViewModel() {

    val batches: StateFlow<List<FodderBatch>> = repository.getAllBatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val purchases: StateFlow<List<FeedPurchase>> = repository.getAllFeedPurchases()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val conditioners: StateFlow<List<ConditionerLog>> = repository.getAllConditioners()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBatch(batch: FodderBatch, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (batch.id.isNotEmpty()) {
                    repository.updateBatch(batch)
                } else {
                    repository.addBatch(batch)
                }
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("FodderViewModel", "Error adding/updating batch", e)
                onResult(false)
            }
        }
    }

    fun addPurchase(purchase: FeedPurchase, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (purchase.id.isNotEmpty()) {
                    repository.updateFeedPurchase(purchase)
                } else {
                    repository.addFeedPurchase(purchase)
                }
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("FodderViewModel", "Error adding/updating purchase", e)
                onResult(false)
            }
        }
    }

    fun addConditioner(log: ConditionerLog, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (log.id.isNotEmpty()) {
                    repository.updateConditioner(log)
                } else {
                    repository.addConditioner(log)
                }
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("FodderViewModel", "Error adding/updating conditioner", e)
                onResult(false)
            }
        }
    }

    fun deleteBatch(id: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteBatch(id)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deletePurchase(id: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteFeedPurchase(id)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteConditioner(id: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteConditioner(id)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
