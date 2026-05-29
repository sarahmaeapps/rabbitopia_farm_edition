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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.sarahmaeapps.rabbitopia.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import coil.compose.AsyncImage
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

    val listings: StateFlow<List<Listing>> = salesRepository.getAllListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forSaleRabbits: StateFlow<List<Rabbit>> = rabbitRepository.getActiveRabbits().map { list ->
        list.filter { it.forSale }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSale(sale: Sale, documentUri: Uri? = null, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                salesRepository.addSale(sale, documentUri)
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("SalesViewModel", "Error adding sale", e)
                onResult(false)
            }
        }
    }

    fun addCullRecord(record: CullRecord, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                cullRepository.addCullRecord(record)
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("SalesViewModel", "Error adding cull record", e)
                onResult(false)
            }
        }
    }

    fun addListing(listing: Listing, imageUri: Uri? = null, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                salesRepository.addListing(listing, imageUri)
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("SalesViewModel", "Error adding listing", e)
                onResult(false)
            }
        }
    }

    fun deleteListing(id: String) {
        viewModelScope.launch { salesRepository.deleteListing(id) }
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
    val listings by viewModel.listings.collectAsState()
    val forSaleRabbits by viewModel.forSaleRabbits.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Sales & Marketplace") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Revenue") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Animal") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Non-Animal") })
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != 1) { // Animals are listed via registry checkbox
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF880015), contentColor = Color.White) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> RevenueTab(sales, customers, rabbits, onNavigateToSaleDetail)
                1 -> AnimalsTab(forSaleRabbits)
                2 -> ItemsTab(listings, onDelete = { viewModel.deleteListing(it) })
            }
        }

        if (showAddDialog) {
            if (selectedTab == 0) {
                AddSaleDialog(
                    customers = customers,
                    rabbits = rabbits,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { sale, uri ->
                        viewModel.addSale(sale, uri)
                        showAddDialog = false
                    }
                )
            } else if (selectedTab == 2) {
                AddItemListingDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { listing, uri, onResult ->
                        viewModel.addListing(listing, uri) { success ->
                            onResult(success)
                            if (success) showAddDialog = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RevenueTab(sales: List<Sale>, customers: List<Customer>, rabbits: List<Rabbit>, onNavigateToSaleDetail: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Animal", "Non-Animal")

    val filteredSales = if (selectedCategory == "All") sales else sales.filter { it.category == selectedCategory }

    Column {
        val totalRevenue = filteredSales.sumOf { it.amount }
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Revenue: $selectedCategory", fontWeight = FontWeight.Bold)
                Text("$${String.format(Locale.getDefault(), "%.2f", totalRevenue)}", fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }

        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {}
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category) }
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredSales) { sale ->
                val customerName = customers.find { it.id == sale.customerId }?.name ?: "Unknown"
                val rabbitName = if (sale.category == "Animal") {
                    rabbits.find { it.id == sale.rabbitId }?.name ?: "Unknown"
                } else {
                    "Non-Animal Item"
                }
                SaleItem(sale, customerName, rabbitName, onClick = { onNavigateToSaleDetail(sale.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun AnimalsTab(rabbits: List<Rabbit>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(rabbits) { rabbit ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rabbit.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(rabbit.breed, fontSize = 12.sp, color = Color.Gray)
                        Text("Price: $${rabbit.salePrice}", fontWeight = FontWeight.Bold, color = Color.Green)
                    }
                    if (rabbit.imagePath != null) {
                        AsyncImage(
                            model = rabbit.imagePath,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemsTab(listings: List<Listing>, onDelete: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(listings) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(item.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 2)
                        Text("Price: $${item.price}", fontWeight = FontWeight.Bold, color = Color.Green)
                    }
                    if (item.imagePath != null) {
                        AsyncImage(
                            model = item.imagePath,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemListingDialog(onDismiss: () -> Unit, onConfirm: (Listing, Uri?, (Boolean) -> Unit) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedUri = it }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Misc Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, enabled = !isSaving)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 3, enabled = !isSaving)
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price ($)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), enabled = !isSaving)
                
                Button(
                    onClick = { launcher.launch("image/*") }, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text(if (selectedUri == null) "Add Photo" else "Photo Attached ✅")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (isSaving) return@Button
                    isSaving = true
                    onConfirm(Listing(
                        name = name, 
                        description = description, 
                        price = price.toDoubleOrNull() ?: 0.0
                    ), selectedUri) { success ->
                        isSaving = false
                        if (!success) {
                            Toast.makeText(context, "Failed to save listing. Check permissions.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isSaving && name.isNotEmpty() && price.isNotEmpty()
            ) { 
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Post Listing")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } }
    )
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
                        rabbitId = if (isSaleMode) selectedRabbitId else "SYSTEM_DOC",
                        category = "Animal"
                    ), selectedUri)
                },
                enabled = (isSaleMode && selectedCustomerId.isNotEmpty() && selectedRabbitId.isNotEmpty() && amount.isNotEmpty()) || (!isSaleMode && selectedUri != null)
            ) { Text(if (isSaleMode) "Record" else "Upload") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
