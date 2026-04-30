package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
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
import com.sarahmaeapps.rabbitopia.data.BreedingRepository
import com.sarahmaeapps.rabbitopia.model.BreedingEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BreedingHistoryViewModel(
    private val repository: BreedingRepository = BreedingRepository(),
    private val rabbitId: String
) : ViewModel() {
    val breedingEvents: StateFlow<List<BreedingEvent>> = repository.getAllBreedingEvents()
        .map { events -> 
            events.filter { it.damId == rabbitId || it.sireId == rabbitId } 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteBreedingEvent(id: String) {
        viewModelScope.launch { repository.deleteBreedingEvent(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingHistoryScreen(
    rabbitId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val viewModel: BreedingHistoryViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BreedingHistoryViewModel(rabbitId = rabbitId) as T
        }
    })
    val events by viewModel.breedingEvents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breeding History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAdd(rabbitId) }, containerColor = Color(0xFF880015), contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Breeding")
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No breeding events recorded.", color = Color.White)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(events) { event ->
                    BreedingEventItem(event, onClick = { onNavigateToDetail(event.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun BreedingEventItem(event: BreedingEvent, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Mate: ${event.sireId}", fontWeight = FontWeight.Bold)
            Text(text = sdf.format(Date(event.breedingDate)), fontSize = 12.sp, color = Color.Gray)
        }
        Text(text = "Due: ${sdf.format(Date(event.dueDate))}", color = Color.White)
        if (event.notes.isNotEmpty()) {
            Text(text = event.notes, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
        }
    }
}
