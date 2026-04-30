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

    fun addBatch(batch: FodderBatch) {
        viewModelScope.launch {
            if (batch.id.isNotEmpty()) {
                repository.updateBatch(batch)
            } else {
                repository.addBatch(batch)
            }
        }
    }

    fun addPurchase(purchase: FeedPurchase) {
        viewModelScope.launch {
            if (purchase.id.isNotEmpty()) {
                repository.updateFeedPurchase(purchase)
            } else {
                repository.addFeedPurchase(purchase)
            }
        }
    }

    fun addConditioner(log: ConditionerLog) {
        viewModelScope.launch {
            if (log.id.isNotEmpty()) {
                repository.updateConditioner(log)
            } else {
                repository.addConditioner(log)
            }
        }
    }

    fun deleteBatch(id: String) {
        viewModelScope.launch { repository.deleteBatch(id) }
    }

    fun deletePurchase(id: String) {
        viewModelScope.launch { repository.deleteFeedPurchase(id) }
    }

    fun deleteConditioner(id: String) {
        viewModelScope.launch { repository.deleteConditioner(id) }
    }
}
