package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.model.FodderBatch
import com.sarahmaeapps.rabbitopia.ui.viewmodel.FodderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FodderScreen(
    viewModel: FodderViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val batches by viewModel.batches.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val conditioners by viewModel.conditioners.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Any?>(null) }

    val totalSpend = batches.sumOf { it.cost } + purchases.sumOf { it.price } + conditioners.sumOf { it.cost }
    
    // Calculate monthly average (last 30 days)
    val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
    val monthlySpend = batches.filter { it.startDate > oneMonthAgo }.sumOf { it.cost } +
            purchases.filter { it.purchaseDate > oneMonthAgo }.sumOf { it.price } +
            conditioners.filter { it.date > oneMonthAgo }.sumOf { it.cost }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Feed & Nutrition Management") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                
                // Spend Stats Header
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF880015))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Spend", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("$${String.format(Locale.getDefault(), "%.2f", totalSpend)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Monthly Avg", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("$${String.format(Locale.getDefault(), "%.2f", monthlySpend)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Fodder") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Store Feed") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Supplements") })
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF880015), contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> FodderTab(batches, onEdit = { editingItem = it })
                1 -> StoreFeedTab(purchases, onEdit = { editingItem = it })
                2 -> SupplementsTab(conditioners, onEdit = { editingItem = it })
            }
        }

        if (showAddDialog) {
            when (selectedTab) {
                0 -> AddFodderDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.addBatch(it); showAddDialog = false })
                1 -> AddFeedPurchaseDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.addPurchase(it); showAddDialog = false })
                2 -> AddConditionerDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.addConditioner(it); showAddDialog = false })
            }
        }

        editingItem?.let { item ->
            when (item) {
                is FodderBatch -> AddFodderDialog(
                    initialBatch = item, 
                    onDismiss = { editingItem = null }, 
                    onConfirm = { viewModel.addBatch(it); editingItem = null },
                    onDelete = { viewModel.deleteBatch(item.id); editingItem = null }
                )
                is com.sarahmaeapps.rabbitopia.model.FeedPurchase -> AddFeedPurchaseDialog(
                    initialPurchase = item,
                    onDismiss = { editingItem = null },
                    onConfirm = { viewModel.addPurchase(it); editingItem = null },
                    onDelete = { viewModel.deletePurchase(item.id); editingItem = null }
                )
                is com.sarahmaeapps.rabbitopia.model.ConditionerLog -> AddConditionerDialog(
                    initialLog = item,
                    onDismiss = { editingItem = null },
                    onConfirm = { viewModel.addConditioner(it); editingItem = null },
                    onDelete = { viewModel.deleteConditioner(item.id); editingItem = null }
                )
            }
        }
    }
}

@Composable
fun FodderTab(batches: List<FodderBatch>, onEdit: (FodderBatch) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nutritional Peak Note", fontWeight = FontWeight.Bold)
                    Text("Fodder is most nutritious at Day 6. Alerts will show when a batch hits peak.")
                }
            }
        }
        items(batches) { batch ->
            Box(modifier = Modifier.clickable { onEdit(batch) }) {
                FodderBatchItem(batch)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun StoreFeedTab(purchases: List<com.sarahmaeapps.rabbitopia.model.FeedPurchase>, onEdit: (com.sarahmaeapps.rabbitopia.model.FeedPurchase) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(purchases) { purchase ->
            Column(modifier = Modifier.fillMaxWidth().clickable { onEdit(purchase) }.padding(16.dp)) {
                Text(purchase.brand, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Supplier: ${purchase.supplier}")
                Text("Stats: ${purchase.proteinPercent}% Protein, ${purchase.fiberPercent}% Fiber, ${purchase.fatPercent}% Fat")
                Text("Bought: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(purchase.purchaseDate))} - ${purchase.weightLbs} lbs @ \$${purchase.price}")
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun SupplementsTab(conditioners: List<com.sarahmaeapps.rabbitopia.model.ConditionerLog>, onEdit: (com.sarahmaeapps.rabbitopia.model.ConditionerLog) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(conditioners) { log ->
            ListItem(
                modifier = Modifier.clickable { onEdit(log) },
                headlineContent = { Text(log.type) },
                supportingContent = { Text("${log.notes} | Cost: \$${log.cost}") },
                overlineContent = { Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(log.date))) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun AddFeedPurchaseDialog(
    initialPurchase: com.sarahmaeapps.rabbitopia.model.FeedPurchase? = null,
    onDismiss: () -> Unit, 
    onConfirm: (com.sarahmaeapps.rabbitopia.model.FeedPurchase) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var brand by remember { mutableStateOf(initialPurchase?.brand ?: "") }
    var supplier by remember { mutableStateOf(initialPurchase?.supplier ?: "") }
    var price by remember { mutableStateOf(initialPurchase?.price?.toString() ?: "") }
    var weight by remember { mutableStateOf(initialPurchase?.weightLbs?.toString() ?: "") }
    var protein by remember { mutableStateOf(initialPurchase?.proteinPercent?.toString() ?: "") }
    var fiber by remember { mutableStateOf(initialPurchase?.fiberPercent?.toString() ?: "") }
    var fat by remember { mutableStateOf(initialPurchase?.fatPercent?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialPurchase == null) "New Feed Purchase" else "Edit Feed Purchase") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") })
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Lbs") }, modifier = Modifier.weight(1f))
                }
                Text("Formulation (%)", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Prot") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fiber, onValueChange = { fiber = it }, label = { Text("Fib") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(com.sarahmaeapps.rabbitopia.model.FeedPurchase(
                    id = initialPurchase?.id ?: "",
                    brand = brand,
                    supplier = supplier,
                    price = price.toDoubleOrNull() ?: 0.0,
                    weightLbs = weight.toDoubleOrNull() ?: 0.0,
                    proteinPercent = protein.toDoubleOrNull() ?: 0.0,
                    fiberPercent = fiber.toDoubleOrNull() ?: 0.0,
                    fatPercent = fat.toDoubleOrNull() ?: 0.0
                ))
            }) { Text("Save") }
        },
        dismissButton = { 
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = Color.Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun AddConditionerDialog(
    initialLog: com.sarahmaeapps.rabbitopia.model.ConditionerLog? = null,
    onDismiss: () -> Unit, 
    onConfirm: (com.sarahmaeapps.rabbitopia.model.ConditionerLog) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var type by remember { mutableStateOf(initialLog?.type ?: "") }
    var notes by remember { mutableStateOf(initialLog?.notes ?: "") }
    var cost by remember { mutableStateOf(initialLog?.cost?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialLog == null) "Log Supplement" else "Edit Supplement Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (BOSS, Oats, etc.)") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(com.sarahmaeapps.rabbitopia.model.ConditionerLog(
                    id = initialLog?.id ?: "",
                    type = type, 
                    notes = notes,
                    cost = cost.toDoubleOrNull() ?: 0.0
                )) 
            }) { Text(if (initialLog == null) "Log" else "Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = Color.Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun FodderBatchItem(batch: FodderBatch) {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val startDate = sdf.format(Date(batch.startDate))
    
    // Day 6 check for nutritional peak
    val ageDays = ((System.currentTimeMillis() - batch.startDate) / (1000 * 60 * 60 * 24)).toInt()
    val isPeak = ageDays == 6

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Batch: ${batch.batchID}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (isPeak) {
                Surface(color = Color(0xFF4CAF50), shape = MaterialTheme.shapes.small) {
                    Text("NUTRITIONAL PEAK", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp)
                }
            }
        }
        Text("Started: $startDate (Day $ageDays)")
        Text("Temp: ${batch.temperature}°F", color = if (batch.temperature > 80) Color.Red else Color.Unspecified)
        Text("Yield: ${batch.yieldWeight} lbs | Cost: \$${batch.cost}")
    }
}

@Composable
fun AddFodderDialog(
    initialBatch: FodderBatch? = null,
    onDismiss: () -> Unit, 
    onConfirm: (FodderBatch) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var batchId by remember { mutableStateOf(initialBatch?.batchID ?: "") }
    var temp by remember { mutableStateOf(initialBatch?.temperature?.toString() ?: "") }
    var cost by remember { mutableStateOf(initialBatch?.cost?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBatch == null) "New Fodder Batch" else "Edit Fodder Batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = batchId, onValueChange = { batchId = it }, label = { Text("Batch ID") })
                OutlinedTextField(value = temp, onValueChange = { temp = it }, label = { Text("Barn Temp (°F)") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Setup Cost ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(FodderBatch(
                    id = initialBatch?.id ?: "",
                    batchID = batchId,
                    temperature = temp.toDoubleOrNull() ?: 0.0,
                    cost = cost.toDoubleOrNull() ?: 0.0,
                    startDate = initialBatch?.startDate ?: System.currentTimeMillis()
                ))
            }) { Text(if (initialBatch == null) "Start Batch" else "Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = Color.Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
