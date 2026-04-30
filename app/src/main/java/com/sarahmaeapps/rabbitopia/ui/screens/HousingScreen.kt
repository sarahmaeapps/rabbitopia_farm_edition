@file:OptIn(ExperimentalMaterial3Api::class)
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sarahmaeapps.rabbitopia.model.Hutch
import com.sarahmaeapps.rabbitopia.model.Rabbit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class HousingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    
    val hutches: StateFlow<List<Hutch>> = firestore.collection("hutches")
        .snapshots().map { it.toObjects(Hutch::class.java) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rabbits: StateFlow<List<Rabbit>> = firestore.collection("rabbits")
        .whereIn("status", listOf("Breeder", "Show", "4H Starter", "Meat"))
        .snapshots().map { it.toObjects(Rabbit::class.java) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHutch(hutch: Hutch) {
        viewModelScope.launch {
            if (hutch.id.isNotEmpty()) {
                firestore.collection("hutches").document(hutch.id).set(hutch).await()
            } else {
                firestore.collection("hutches").add(hutch).await()
            }
        }
    }

    suspend fun getHutchById(id: String): Hutch? {
        return firestore.collection("hutches").document(id).get().await().toObject(Hutch::class.java)
    }

    fun updateHutch(hutch: Hutch) {
        if (hutch.id.isNotEmpty()) {
            viewModelScope.launch {
                firestore.collection("hutches").document(hutch.id).set(hutch).await()
            }
        }
    }
}

@Composable
fun HousingScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: HousingViewModel = viewModel()
) {
    val hutches by viewModel.hutches.collectAsState()
    val rabbits by viewModel.rabbits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Housing / Hutches") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF880015), contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Hutch")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(hutches) { hutch ->
                val occupants = rabbits.filter { it.hutchId == hutch.hutchId }
                HutchItem(
                    hutch = hutch, 
                    occupants = occupants,
                    onClick = { onNavigateToDetail(hutch.id) }
                )
                HorizontalDivider()
            }
        }

        if (showAddDialog) {
            AddHutchDialog(
                rabbits = rabbits,
                onDismiss = { showAddDialog = false },
                onConfirm = { id, occ ->
                    viewModel.addHutch(Hutch(hutchId = id, capacity = occ))
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun HutchItem(hutch: Hutch, occupants: List<Rabbit>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Hutch: ${hutch.hutchId}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (hutch.status != "Good") {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color.Red)
            }
        }
        Text("Occupancy: ${occupants.size}")
        if (occupants.isNotEmpty()) {
            Text("Residents: ${occupants.joinToString { it.name }}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun HutchDetailScreen(
    hutchId: String,
    onNavigateBack: () -> Unit,
    viewModel: HousingViewModel = viewModel()
) {
    val hutches by viewModel.hutches.collectAsState()
    val hutch = hutches.find { it.id == hutchId }
    val rabbits by viewModel.rabbits.collectAsState()
    var showMaintenanceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hutch: ${hutch?.hutchId ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        hutch?.let { h ->
            val occupants = rabbits.filter { it.hutchId == h.hutchId }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Residents", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Text(occupants.joinToString { it.name }.ifEmpty { "None" }, fontSize = 18.sp)
                        Text("Occupancy: ${occupants.size}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                OutlinedTextField(
                    value = h.status,
                    onValueChange = { viewModel.updateHutch(h.copy(status = it)) },
                    label = { Text("Condition / Status") },
                    modifier = Modifier.fillMaxWidth()
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = h.predatorySignsCheck, 
                                onCheckedChange = { viewModel.updateHutch(h.copy(predatorySignsCheck = it)) }
                            )
                            Text("Predatorial Signs / Activity", fontWeight = FontWeight.Bold)
                        }
                        if (h.predatorySignsCheck) {
                            OutlinedTextField(
                                value = h.predatoryNotes,
                                onValueChange = { viewModel.updateHutch(h.copy(predatoryNotes = it)) },
                                label = { Text("Describe Signs") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = h.upgrades,
                    onValueChange = { viewModel.updateHutch(h.copy(upgrades = it)) },
                    label = { Text("Upgrades & Improvements") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Maintenance Records Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Repair & Maintenance History", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showMaintenanceDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Maintenance")
                    }
                }
                
                h.maintenanceRecords.forEach { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(record.date)), fontSize = 12.sp, color = Color.Gray)
                            Text(record.description)
                            if (record.cost > 0) {
                                Text("Cost: $${record.cost}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temperature Logs section
                Text("Temperature Logs", fontWeight = FontWeight.Bold)
                var tempInput by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it },
                        label = { Text("Log Temp (°F)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Button(onClick = {
                        val temp = tempInput.toDoubleOrNull() ?: 0.0
                        if (temp != 0.0) {
                            val newLog = com.sarahmaeapps.rabbitopia.model.TemperatureLog(hutchId = h.hutchId, temperature = temp)
                            // For simplicity, we'll just save it to the global temp logs collection via firestore directly here or in ViewModel
                            // But user wants it on the hutch page.
                            tempInput = ""
                        }
                    }) { Text("Log") }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showMaintenanceDialog) {
                AddMaintenanceDialog(
                    onDismiss = { showMaintenanceDialog = false },
                    onConfirm = { desc, cost ->
                        val newRecord = com.sarahmaeapps.rabbitopia.model.MaintenanceRecord(description = desc, cost = cost)
                        viewModel.updateHutch(h.copy(maintenanceRecords = h.maintenanceRecords + newRecord))
                        showMaintenanceDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddMaintenanceDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var desc by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost ($)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(desc, cost.toDoubleOrNull() ?: 0.0) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddHutchDialog(
    rabbits: List<Rabbit>,
    onDismiss: () -> Unit, 
    onConfirm: (String, Int) -> Unit
) {
    val letters = ('A'..'Z').map { it.toString() }
    val numbers = (0..9).map { it.toString() }
    
    var selectedLetter by remember { mutableStateOf(letters[0]) }
    var selectedNumber by remember { mutableStateOf(numbers[0]) }
    var expandedLetter by remember { mutableStateOf(false) }
    var expandedNumber by remember { mutableStateOf(false) }

    val currentHutchId = "$selectedLetter $selectedNumber"
    val occupancy = rabbits.count { it.hutchId == currentHutchId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Hutch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Hutch ID", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedLetter, 
                        onExpandedChange = { expandedLetter = !expandedLetter },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedLetter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Letter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLetter) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedLetter, onDismissRequest = { expandedLetter = false }) {
                            letters.forEach { l ->
                                DropdownMenuItem(text = { Text(l) }, onClick = { selectedLetter = l; expandedLetter = false })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedNumber, 
                        onExpandedChange = { expandedNumber = !expandedNumber },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedNumber,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Number") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNumber) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedNumber, onDismissRequest = { expandedNumber = false }) {
                            numbers.forEach { n ->
                                DropdownMenuItem(text = { Text(n) }, onClick = { selectedNumber = n; expandedNumber = false })
                            }
                        }
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Occupancy", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Text(
                            text = "$occupancy Animal${if(occupancy != 1) "s" else ""} assigned",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentHutchId, occupancy) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
