package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.model.Customer
import com.sarahmaeapps.rabbitopia.model.Sale
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToRabbitDetail: (String) -> Unit,
    onNavigateToPedigree: (String) -> Unit,
    viewModel: CustomerViewModel = viewModel(),
    rabbitViewModel: RabbitViewModel = viewModel()
) {
    var customer by remember { mutableStateOf<Customer?>(null) }
    val sales by viewModel.getSalesForCustomer(customerId).collectAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        customer = viewModel.getCustomerById(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {
                        viewModel.deleteCustomer(customerId)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                    IconButton(onClick = { 
                        customer?.let { onNavigateToChat(it.id.trim().lowercase(), it.name) } 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Message")
                    }
                }
            )
        }
    ) { padding ->
        if (showEditDialog && customer != null) {
            EditCustomerDialog(
                customer = customer!!,
                onDismiss = { showEditDialog = false },
                onConfirm = { updated ->
                    viewModel.addCustomer(updated)
                    customer = updated
                    showEditDialog = false
                }
            )
        }
        customer?.let { c ->
            val lowercaseEmail = c.id.trim().lowercase()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Contact Information", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        DetailInfoRow("Phone", c.phone)
                        DetailInfoRow("Address", c.address)
                        DetailInfoRow("ARBA #", c.arbaNumber)
                        DetailInfoRow("Total Lifetime Value", "$${String.format("%.2f", c.lifetimeValue)}")
                    }
                }

                Text(text = "Purchase History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                if (sales.isEmpty()) {
                    Text("No purchases recorded yet.")
                } else {
                    sales.forEach { sale ->
                        var rabbitName by remember { mutableStateOf("Loading...") }
                        LaunchedEffect(sale.rabbitId) {
                            rabbitName = rabbitViewModel.getRabbitById(sale.rabbitId)?.name ?: "Unknown Rabbit"
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = rabbitName, fontWeight = FontWeight.Bold)
                                    Text(text = "$${sale.amount}", fontWeight = FontWeight.ExtraBold)
                                }
                                Text(text = "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(sale.date))}")
                                
                                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onNavigateToRabbitDetail(sale.rabbitId) }) {
                                        Text("View Animal")
                                    }
                                    TextButton(onClick = { onNavigateToPedigree(sale.rabbitId) }) {
                                        Text("View Pedigree")
                                    }
                                }
                            }
                        }
                    }
                }

                Text(text = "Notes", fontWeight = FontWeight.Bold, color = Color.White)
                OutlinedTextField(
                    value = c.notes,
                    onValueChange = { /* Update notes logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { 
                        val encodedEmail = java.net.URLEncoder.encode(lowercaseEmail, "UTF-8")
                        onNavigateToChat(encodedEmail, c.name) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Messages", fontWeight = FontWeight.Bold)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun EditCustomerDialog(customer: Customer, onDismiss: () -> Unit, onConfirm: (Customer) -> Unit) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var address by remember { mutableStateOf(customer.address) }
    var arba by remember { mutableStateOf(customer.arbaNumber) }
    var notes by remember { mutableStateOf(customer.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                OutlinedTextField(value = arba, onValueChange = { arba = it }, label = { Text("ARBA #") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(customer.copy(name = name, phone = phone, address = address, arbaNumber = arba, notes = notes)) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = value.ifEmpty { "N/A" }, color = Color.White)
    }
}
