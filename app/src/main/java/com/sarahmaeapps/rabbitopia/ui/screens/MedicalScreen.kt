package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.sarahmaeapps.rabbitopia.data.MedicalRepository
import com.sarahmaeapps.rabbitopia.data.RabbitRepository
import com.sarahmaeapps.rabbitopia.model.MedicalRecord
import com.sarahmaeapps.rabbitopia.model.Rabbit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicalViewModel(
    private val medicalRepository: MedicalRepository = MedicalRepository(),
    private val rabbitRepository: RabbitRepository = RabbitRepository()
) : ViewModel() {
    val records: StateFlow<List<MedicalRecord>> = medicalRepository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rabbits: StateFlow<List<Rabbit>> = rabbitRepository.getActiveRabbits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecord(record: MedicalRecord) {
        viewModelScope.launch {
            if (record.id.isNotEmpty()) {
                medicalRepository.updateRecord(record)
            } else {
                medicalRepository.addRecord(record)
            }
        }
    }

    suspend fun getRecordById(id: String): MedicalRecord? {
        return medicalRepository.getRecordById(id)
    }

    fun updateRecord(record: MedicalRecord) {
        viewModelScope.launch { medicalRepository.updateRecord(record) }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch { medicalRepository.deleteRecord(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MedicalViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val rabbits by viewModel.rabbits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("Medical") }

    val totalCost = records.sumOf { it.cost }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical / Health Vault") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Total cost header
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF880015))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Medical/Health Spend", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.getDefault(), "%.2f", totalCost)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { dialogType = "Health"; showAddDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015))
                ) {
                    Text("Record Health Event", fontSize = 12.sp)
                }
                Button(
                    onClick = { dialogType = "Non-Health"; showAddDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Record Non Health Expense", fontSize = 12.sp)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(records) { record ->
                    val rabbit = rabbits.find { it.id == record.rabbitId }
                    MedicalRecordItem(
                        record = record, 
                        rabbitName = rabbit?.name ?: "Unknown",
                        onClick = { onNavigateToDetail(record.id) }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showAddDialog) {
            AddMedicalDialog(
                rabbits = rabbits,
                type = dialogType,
                onDismiss = { showAddDialog = false },
                onConfirm = { record ->
                    viewModel.addRecord(record)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MedicalRecordItem(record: MedicalRecord, rabbitName: String, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = rabbitName, fontWeight = FontWeight.Bold)
            Text(text = "$${String.format("%.2f", record.cost)}", fontWeight = FontWeight.Bold, color = Color.Green)
        }
        Text(text = "[${record.type}] ${record.condition}", color = Color.White)
        Text(text = "Treatment: ${record.treatment}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        Text(text = sdf.format(Date(record.date)), fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicalDialog(
    rabbits: List<Rabbit>,
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (MedicalRecord) -> Unit
) {
    var rabbitId by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var isCullingIssue by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == "Health") "Record Health Event" else "Record Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    val rabbitName = rabbits.find { it.id == rabbitId }?.name ?: "Select Rabbit"
                    OutlinedTextField(
                        value = rabbitName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rabbit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        rabbits.forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { rabbitId = r.id; expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = condition, onValueChange = { condition = it }, label = { Text(if (type == "Health") "Diagnosis" else "Description") })
                OutlinedTextField(value = treatment, onValueChange = { treatment = it }, label = { Text(if (type == "Health") "Treatment Course" else "Action/Notes") })
                OutlinedTextField(
                    value = cost, 
                    onValueChange = { cost = it }, 
                    label = { Text("Cost ($)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                if (type == "Health") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isCullingIssue, onCheckedChange = { isCullingIssue = it })
                        Text("Culling Defect (Malocclusion, etc.)")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(MedicalRecord(
                    rabbitId = rabbitId,
                    condition = condition,
                    treatment = treatment,
                    cost = cost.toDoubleOrNull() ?: 0.0,
                    isCullingIssue = isCullingIssue,
                    type = type
                ))
            }) { Text("Record") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDetailScreen(
    recordId: String,
    onNavigateBack: () -> Unit,
    viewModel: MedicalViewModel = viewModel()
) {
    var record by remember { mutableStateOf<MedicalRecord?>(null) }
    val rabbits by viewModel.rabbits.collectAsState()

    LaunchedEffect(recordId) {
        record = viewModel.getRecordById(recordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteRecord(recordId)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        record?.let { r ->
            val rabbitName = rabbits.find { it.id == r.rabbitId }?.name ?: "Unknown"
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Rabbit: $rabbitName", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "Type: ${r.type}", fontWeight = FontWeight.Medium)
                Text(text = "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(r.date))}")
                
                OutlinedTextField(
                    value = r.condition,
                    onValueChange = { viewModel.updateRecord(r.copy(condition = it)) },
                    label = { Text(if (r.type == "Health") "Diagnosis" else "Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = r.cost.toString(),
                    onValueChange = { val value = it.toDoubleOrNull() ?: 0.0; viewModel.updateRecord(r.copy(cost = value)) },
                    label = { Text("Cost ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = r.treatment,
                    onValueChange = { viewModel.updateRecord(r.copy(treatment = it)) },
                    label = { Text(if (r.type == "Health") "Treatment Course" else "Action/Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = r.medications,
                    onValueChange = { viewModel.updateRecord(r.copy(medications = it)) },
                    label = { Text("Medications") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = r.vetNotes,
                    onValueChange = { viewModel.updateRecord(r.copy(vetNotes = it)) },
                    label = { Text("Vet Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (r.isCullingIssue) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            "Warning: This is a culling defect. Animal should be removed from 4th generation project.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
