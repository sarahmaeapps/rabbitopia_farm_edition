package com.sarahmaeapps.rabbitopia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahmaeapps.rabbitopia.data.RabbitRepository
import com.sarahmaeapps.rabbitopia.model.Rabbit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri

class RabbitViewModel(private val repository: RabbitRepository = RabbitRepository()) : ViewModel() {

    val rabbits: StateFlow<List<Rabbit>> = repository.getActiveRabbits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val culledRabbits: StateFlow<List<Rabbit>> = repository.getCulledRabbits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val breederBucks: StateFlow<List<Rabbit>> = repository.getBreederBucks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val breederDoes: StateFlow<List<Rabbit>> = repository.getBreederDoes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRabbit(rabbit: Rabbit, imageUri: Uri? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (rabbit.id.isNotEmpty()) {
                    repository.updateRabbit(rabbit, imageUri)
                } else {
                    if (imageUri != null) {
                        repository.addRabbitWithImage(rabbit, imageUri)
                    } else {
                        repository.addRabbit(rabbit)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("RabbitViewModel", "Error adding/updating rabbit", e)
            } finally {
                onComplete()
            }
        }
    }

    suspend fun getRabbitById(id: String): Rabbit? {
        return repository.getRabbitById(id)
    }

    fun getRabbitFlow(id: String): kotlinx.coroutines.flow.Flow<Rabbit?> {
        return repository.getRabbitFlow(id)
    }

    fun deleteRabbit(id: String) {
        viewModelScope.launch { repository.deleteRabbit(id) }
    }
}
