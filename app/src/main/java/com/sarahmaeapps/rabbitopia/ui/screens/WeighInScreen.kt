package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sarahmaeapps.rabbitopia.data.WeighInRepository
import com.sarahmaeapps.rabbitopia.model.WeighInRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeighInViewModel(
    private val repository: WeighInRepository = WeighInRepository(),
    private val rabbitId: String
) : ViewModel() {
    val weighIns: StateFlow<List<WeighInRecord>> = repository.getWeighInsForRabbit(rabbitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWeighIn(weight: Double, notes: String) {
        viewModelScope.launch {
            repository.addWeighIn(WeighInRecord(rabbitId = rabbitId, weight = weight, notes = notes))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighInScreen(rabbitId: String, onNavigateBack: () -> Unit) {
    val viewModel: WeighInViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeighInViewModel(rabbitId = rabbitId) as T
        }
    })
    
    val weighIns by viewModel.weighIns.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weigh-In History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Weigh-In")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weighIns.sortedByDescending { it.date }) { record ->
                WeighInItem(record)
                HorizontalDivider()
            }
        }

        if (showAddDialog) {
            AddWeighInDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { weight, notes ->
                    viewModel.addWeighIn(weight, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun WeighInItem(record: WeighInRecord) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "${record.weight} lbs", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = sdf.format(Date(record.date)), color = Color.Gray)
        }
        if (record.notes.isNotEmpty()) {
            Text(text = record.notes, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun AddWeighInDialog(onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Weigh-In") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (lbs)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(weight.toDoubleOrNull() ?: 0.0, notes) }) { Text("Record") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
