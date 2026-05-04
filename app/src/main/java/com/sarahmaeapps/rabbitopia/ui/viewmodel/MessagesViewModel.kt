package com.sarahmaeapps.rabbitopia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahmaeapps.rabbitopia.data.ChatRepository
import com.sarahmaeapps.rabbitopia.data.CustomerRepository
import com.sarahmaeapps.rabbitopia.model.Customer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ChatThread(
    val email: String,
    val customerName: String,
    val hasUnread: Boolean = false
)

class MessagesViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val customerRepository: CustomerRepository = CustomerRepository()
) : ViewModel() {

    private val adminEmail = "rabbitopiafarm@gmail.com"

    val chatThreads: StateFlow<List<ChatThread>> = combine(
        chatRepository.getAllChatThreads(),
        customerRepository.getAllCustomers(),
        chatRepository.getUnreadMessages(adminEmail)
    ) { threadEmails, customers, unreadMessages ->
        threadEmails.map { email ->
            val customer = customers.find { it.email.lowercase() == email.lowercase() }
            val hasUnread = unreadMessages.any { it.senderId.lowercase() == email.lowercase() }
            ChatThread(
                email = email,
                customerName = customer?.name ?: "New Inquiry ($email)",
                hasUnread = hasUnread
            )
        }.sortedByDescending { it.hasUnread }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
