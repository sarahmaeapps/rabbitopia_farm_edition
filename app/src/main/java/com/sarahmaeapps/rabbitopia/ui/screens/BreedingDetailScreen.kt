package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.sarahmaeapps.rabbitopia.data.BreedingRepository
import com.sarahmaeapps.rabbitopia.model.BreedingEvent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BreedingDetailViewModel(private val repository: BreedingRepository = BreedingRepository()) : ViewModel() {
    fun deleteEvent(id: String) {
        viewModelScope.launch { repository.deleteBreedingEvent(id) }
    }
    
    suspend fun getEventById(id: String): BreedingEvent? {
        return repository.getBreedingEventById(id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: BreedingDetailViewModel = viewModel()
) {
    var event by remember { mutableStateOf<BreedingEvent?>(null) }
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LaunchedEffect(eventId) {
        event = viewModel.getEventById(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breeding Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteEvent(eventId)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        event?.let { e ->
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
                        Text("Breeding Info", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dam (Mother): ${e.damId}")
                        Text("Sire (Father): ${e.sireId}")
                        Text("Breeding Date: ${sdf.format(Date(e.breedingDate))}")
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Timeline", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Estimated Due Date: ${sdf.format(Date(e.dueDate))}", fontWeight = FontWeight.Bold)
                        Text("Estimated Weaning Date: ${sdf.format(Date(e.dueDate + (56L * 24 * 60 * 60 * 1000)))}")
                    }
                }

                if (e.notes.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(e.notes)
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
