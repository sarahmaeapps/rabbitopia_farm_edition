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
import com.sarahmaeapps.rabbitopia.data.LitterRepository
import com.sarahmaeapps.rabbitopia.model.Litter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LitterDetailViewModel(
    private val repository: LitterRepository = LitterRepository()
) : ViewModel() {
    suspend fun getLitterById(id: String): Litter? {
        return repository.getLitterById(id)
    }

    fun deleteLitter(id: String) {
        viewModelScope.launch { repository.deleteLitter(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitterDetailScreen(
    litterId: String,
    onNavigateBack: () -> Unit,
    viewModel: LitterDetailViewModel = viewModel()
) {
    var litter by remember { mutableStateOf<Litter?>(null) }

    LaunchedEffect(litterId) {
        litter = viewModel.getLitterById(litterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Litter Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteLitter(litterId)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        litter?.let { l ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Litter Summary", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Text(text = "Date Kindled: ${sdf.format(Date(l.dateKindled))}")
                        Text(text = "Mother (Dam) ID: ${l.damId}")
                        HorizontalDivider()
                        Text(text = "Total Kit Count: ${l.kitCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Born Alive: ${l.bornAlive}", color = Color.Green)
                        Text(text = "Stillborn: ${l.stillborn}", color = Color.Red)
                        HorizontalDivider()
                        Text(text = "Mother's Care Score: ${l.mothersCareScore} / 10", fontWeight = FontWeight.Medium)
                    }
                }

                Text(text = "Notes", fontWeight = FontWeight.Bold)
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = l.notes.ifEmpty { "No notes recorded for this litter." },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
