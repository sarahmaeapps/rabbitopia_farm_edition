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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.data.CullRepository
import com.sarahmaeapps.rabbitopia.data.CustomerRepository
import com.sarahmaeapps.rabbitopia.data.RabbitRepository
import com.sarahmaeapps.rabbitopia.data.SalesRepository
import com.sarahmaeapps.rabbitopia.model.CullRecord
import com.sarahmaeapps.rabbitopia.model.Customer
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.model.Sale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel(
    private val salesRepository: SalesRepository = SalesRepository(),
    private val customerRepository: CustomerRepository = CustomerRepository(),
    private val rabbitRepository: RabbitRepository = RabbitRepository(),
    private val cullRepository: CullRepository = CullRepository()
) : ViewModel() {
    val sales: StateFlow<List<Sale>> = salesRepository.getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerRepository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rabbits: StateFlow<List<Rabbit>> = rabbitRepository.getActiveRabbits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSale(sale: Sale, documentUri: Uri? = null) {
        viewModelScope.launch { salesRepository.addSale(sale, documentUri) }
    }

    fun addCullRecord(record: CullRecord) {
        viewModelScope.launch { cullRepository.addCullRecord(record) }
    }

    suspend fun getSaleById(id: String): Sale? {
        return salesRepository.getSaleById(id)
    }

    suspend fun getCustomerById(id: String): Customer? {
        return customerRepository.getCustomerById(id)
    }

    fun deleteSale(id: String) {
        viewModelScope.launch { salesRepository.deleteSale(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateToSaleDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = viewModel()
) {
    val sales by viewModel.sales.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val rabbits by viewModel.rabbits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales & Records") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Sale")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val totalRevenue = sales.sumOf { it.amount }
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Revenue", fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.getDefault(), "%.2f", totalRevenue)}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sales) { sale ->
                    val customerName = customers.find { it.id == sale.customerId }?.name ?: "Unknown"
                    val rabbitName = rabbits.find { it.id == sale.rabbitId }?.name ?: "Unknown"
                    SaleItem(sale, customerName, rabbitName, onClick = { onNavigateToSaleDetail(sale.id) })
                    HorizontalDivider()
                }
            }
        }

        if (showAddDialog) {
            AddSaleDialog(
                customers = customers,
                rabbits = rabbits,
                onDismiss = { showAddDialog = false },
                onConfirm = { sale, uri ->
                    viewModel.addSale(sale, uri)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun SaleItem(sale: Sale, customerName: String, rabbitName: String, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = customerName, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "$${sale.amount}", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(text = "Rabbit: $rabbitName", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        Text(text = sdf.format(Date(sale.date)), fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(
    customers: List<Customer>,
    rabbits: List<Rabbit>,
    onDismiss: () -> Unit,
    onConfirm: (Sale, Uri?) -> Unit
) {
    var isSaleMode by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf("") }
    var selectedRabbitId by remember { mutableStateOf("") }
    var customerExpanded by remember { mutableStateOf(false) }
    var rabbitExpanded by remember { mutableStateOf(false) }
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedUri = it
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSaleMode) "Record Sale & Document" else "Upload/Scan Document") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isSaleMode,
                        onClick = { isSaleMode = true },
                        label = { Text("Record Sale") }
                    )
                    FilterChip(
                        selected = !isSaleMode,
                        onClick = { isSaleMode = false },
                        label = { Text("Upload Only") }
                    )
                }

                if (isSaleMode) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )

                    // Customer Selection
                    ExposedDropdownMenuBox(
                        expanded = customerExpanded,
                        onExpandedChange = { customerExpanded = !customerExpanded }
                    ) {
                        val customerName = customers.find { it.id == selectedCustomerId }?.name ?: "Select Customer"
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Customer") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = customerExpanded,
                            onDismissRequest = { customerExpanded = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text(customer.name) },
                                    onClick = {
                                        selectedCustomerId = customer.id
                                        customerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Rabbit Selection
                    ExposedDropdownMenuBox(
                        expanded = rabbitExpanded,
                        onExpandedChange = { rabbitExpanded = !rabbitExpanded }
                    ) {
                        val rabbitName = rabbits.find { it.id == selectedRabbitId }?.name ?: "Select Rabbit"
                        OutlinedTextField(
                            value = rabbitName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rabbit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rabbitExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = rabbitExpanded,
                            onDismissRequest = { rabbitExpanded = false }
                        ) {
                            rabbits.forEach { rabbit ->
                                DropdownMenuItem(
                                    text = { Text("${rabbit.earTattoo} - ${rabbit.name}") },
                                    onClick = {
                                        selectedRabbitId = rabbit.id
                                        rabbitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("Select a photo or scan of your document (Vet Bill, Pedigree, etc.) to save it to your records.", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedUri == null) Color.Gray else Color(0xFF4CAF50))
                ) {
                    Text(if (selectedUri == null) "Scan/Upload Document" else "Document Attached ✅")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(Sale(
                        amount = if (isSaleMode) (amount.toDoubleOrNull() ?: 0.0) else 0.0,
                        customerId = if (isSaleMode) selectedCustomerId else "SYSTEM_DOC",
                        rabbitId = if (isSaleMode) selectedRabbitId else "SYSTEM_DOC"
                    ), selectedUri)
                },
                enabled = (isSaleMode && selectedCustomerId.isNotEmpty() && selectedRabbitId.isNotEmpty() && amount.isNotEmpty()) || (!isSaleMode && selectedUri != null)
            ) { Text(if (isSaleMode) "Record" else "Upload") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
