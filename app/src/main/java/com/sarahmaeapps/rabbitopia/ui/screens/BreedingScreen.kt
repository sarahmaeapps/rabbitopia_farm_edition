package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

class BreedingViewModel(private val repository: BreedingRepository = BreedingRepository()) : ViewModel() {
    fun recordBreeding(damId: String, sireId: String, notes: String) {
        val breedingDate = System.currentTimeMillis()
        val dueDate = breedingDate + (31L * 24 * 60 * 60 * 1000)
        viewModelScope.launch {
            repository.addBreedingEvent(
                BreedingEvent(
                    damId = damId,
                    sireId = sireId,
                    breedingDate = breedingDate,
                    dueDate = dueDate,
                    notes = notes
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingScreen(
    rabbitId: String, 
    onNavigateBack: () -> Unit,
    viewModel: BreedingViewModel = viewModel()
) {
    var mateId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val breedingDate = System.currentTimeMillis()
    val dueDate = breedingDate + (31L * 24 * 60 * 60 * 1000)
    val weaningDate = dueDate + (56L * 24 * 60 * 60 * 1000) // 8 weeks after due date

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Breeding Event") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Dam: $rabbitId", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            OutlinedTextField(
                value = mateId,
                onValueChange = { mateId = it },
                label = { Text("Sire (Mate Ear Tattoo / Name)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Breeding Date: ${sdf.format(Date(breedingDate))}", fontWeight = FontWeight.Bold)
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Auto-Calculations:", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Estimated Due Date (31 days): ${sdf.format(Date(dueDate))}", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Estimated Weaning Date (8 weeks): ${sdf.format(Date(weaningDate))}", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: Alerts will automatically be created for these dates.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Button(
                onClick = { 
                    viewModel.recordBreeding(rabbitId, mateId, notes)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015))
            ) {
                Text("Record Breeding")
            }
        }
    }
}
