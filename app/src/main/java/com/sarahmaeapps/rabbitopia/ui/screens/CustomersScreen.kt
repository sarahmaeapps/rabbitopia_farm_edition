package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.data.CustomerRepository
import com.sarahmaeapps.rabbitopia.data.SalesRepository
import com.sarahmaeapps.rabbitopia.model.Customer
import com.sarahmaeapps.rabbitopia.model.Sale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: CustomerRepository = CustomerRepository(),
    private val salesRepository: SalesRepository = SalesRepository()
) : ViewModel() {
    val customers: StateFlow<List<Customer>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(customer: Customer) {
        viewModelScope.launch { repository.addCustomer(customer) }
    }

    suspend fun getCustomerById(id: String): Customer? {
        return repository.getCustomerById(id)
    }

    fun getSalesForCustomer(customerId: String): Flow<List<Sale>> {
        return salesRepository.getAllSales().map { sales ->
            sales.filter { it.customerId == customerId }
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch { repository.deleteCustomer(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CustomerViewModel = viewModel()
) {
    val customers by viewModel.customers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Database") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(customers) { customer ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCustomerDetail(customer.id) }
                        .padding(16.dp)
                ) {
                    Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text(text = "LTV: $${customer.lifetimeValue}", color = Color.White.copy(alpha = 0.8f))
                    Text(text = "Phone: ${customer.phone}", color = Color.White.copy(alpha = 0.8f))
                }
                HorizontalDivider()
            }
        }

        if (showAddDialog) {
            AddCustomerDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, email, phone, arba, address ->
                    val lowercaseEmail = email.trim().lowercase()
                    viewModel.addCustomer(Customer(
                        name = name, 
                        email = lowercaseEmail, 
                        phone = phone, 
                        arbaNumber = arba,
                        address = address
                    ))
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddCustomerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var arba by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name (Required)") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Required for linking)") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                OutlinedTextField(value = arba, onValueChange = { arba = it }, label = { Text("ARBA #") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, phone, arba, address) },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
