package com.sarahmaeapps.rabbitopia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sarahmaeapps.rabbitopia.data.BreedingRepository
import com.sarahmaeapps.rabbitopia.data.ChatRepository
import com.sarahmaeapps.rabbitopia.model.BreedingEvent
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class Alert(
    val title: String,
    val message: String,
    val type: AlertType
)

enum class AlertType {
    DUE_DATE, WEANING, TEMPERATURE, MESSAGE
}

class HomeViewModel(
    private val repository: BreedingRepository = BreedingRepository(),
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""
    
    val userName: String = auth.currentUser?.displayName ?: "Breeder"

    val alerts: StateFlow<List<Alert>> = combine(
        repository.getAllBreedingEvents(),
        chatRepository.getUnreadMessages(currentUserId)
    ) { events, messages ->
        val activeAlerts = mutableListOf<Alert>()
        val now = System.currentTimeMillis()

        // 1. Breeding Alerts
        events.forEach { event ->
            if (event.status == "Expected") {
                if (event.dueDate > now && event.dueDate - now < 3L * 24 * 60 * 60 * 1000) {
                    activeAlerts.add(Alert("Litter Due Soon!", "Dam ${event.damId} is due on ${formatDate(event.dueDate)}", AlertType.DUE_DATE))
                } else if (event.dueDate < now) {
                    activeAlerts.add(Alert("Overdue Rabbit!", "Dam ${event.damId} was due on ${formatDate(event.dueDate)}", AlertType.DUE_DATE))
                }

                val weaningDate = event.dueDate + (56L * 24 * 60 * 60 * 1000)
                if (weaningDate > now && weaningDate - now < 3L * 24 * 60 * 60 * 1000) {
                    activeAlerts.add(Alert("Weaning Time!", "Litter from ${event.damId} should be weaned by ${formatDate(weaningDate)}", AlertType.WEANING))
                }
            }
        }

        // 2. Message Alerts
        if (messages.isNotEmpty()) {
            val count = messages.size
            activeAlerts.add(Alert("New Messages!", "You have $count unread messages from customers.", AlertType.MESSAGE))
        }

        activeAlerts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
