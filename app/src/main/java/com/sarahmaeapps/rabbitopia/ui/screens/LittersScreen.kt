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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.data.LitterRepository
import com.sarahmaeapps.rabbitopia.data.RabbitRepository
import com.sarahmaeapps.rabbitopia.model.Litter
import com.sarahmaeapps.rabbitopia.model.Rabbit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LittersViewModel(
    private val repository: LitterRepository = LitterRepository(),
    private val rabbitRepository: RabbitRepository = RabbitRepository(),
    private val rabbitId: String
) : ViewModel() {
    val litters: StateFlow<List<Litter>> = repository.getLittersForRabbit(rabbitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val breederBucks: StateFlow<List<Rabbit>> = rabbitRepository.getBreederBucks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getRabbitById(id: String): Rabbit? {
        return rabbitRepository.getRabbitById(id)
    }

    fun addLitter(litter: Litter, kits: List<Rabbit>) {
        viewModelScope.launch {
            repository.addLitter(litter, kits)
        }
    }

    fun deleteLitter(id: String) {
        viewModelScope.launch { repository.deleteLitter(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LittersScreen(
    rabbitId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLitterDetail: (String) -> Unit
) {
    val viewModel: LittersViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LittersViewModel(rabbitId = rabbitId) as T
        }
    })
    
    val litters by viewModel.litters.collectAsState()
    val breederBucks by viewModel.breederBucks.collectAsState()
    var dam by remember { mutableStateOf<Rabbit?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(rabbitId) {
        dam = viewModel.getRabbitById(rabbitId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Litters: ${dam?.name ?: ""}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Litter", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            items(litters) { litter ->
                LitterItem(litter, onClick = { onNavigateToLitterDetail(litter.id) })
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            }
        }

        if (showAddDialog) {
            AddLitterDialog(
                dam = dam,
                breederBucks = breederBucks,
                onDismiss = { showAddDialog = false },
                onConfirm = { litter, kits ->
                    viewModel.addLitter(litter, kits)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun LitterItem(litter: Litter, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Litter ID: #${litter.litterId}", fontWeight = FontWeight.Bold, color = Color.White)
            Text("Date: ${sdf.format(Date(litter.dateKindled))}", color = Color.White)
            Text("Kits: ${litter.kitCount} (${litter.bornAlive} Alive)", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLitterDialog(
    dam: Rabbit?,
    breederBucks: List<Rabbit>,
    onDismiss: () -> Unit,
    onConfirm: (Litter, List<Rabbit>) -> Unit
) {
    var sireId by remember { mutableStateOf("") }
    var bornAliveStr by remember { mutableStateOf("0") }
    var stillbornStr by remember { mutableStateOf("0") }
    var careScore by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }
    var expandedSire by remember { mutableStateOf(false) }

    val options09 = (0..9).map { it.toString() }

    // State for kits
    val bornAlive = bornAliveStr.toIntOrNull() ?: 0
    val kitsData = remember { mutableStateListOf<Rabbit>() }

    // Synchronize kits list with bornAlive count
    LaunchedEffect(bornAlive, sireId, dam) {
        val selectedSire = breederBucks.find { it.id == sireId }
        val calculatedGen = if (selectedSire != null && dam != null) {
            minOf(selectedSire.genCount, dam.genCount) + 1
        } else {
            (dam?.genCount ?: 0) + 1
        }

        while (kitsData.size < bornAlive) {
            kitsData.add(Rabbit(
                name = "Kit ${kitsData.size + 1}",
                sex = "Doe",
                status = "Breeder",
                genCount = calculatedGen,
                damId = dam?.id,
                sireId = sireId,
                motherId = dam?.id,
                fatherId = sireId,
                dateOfBirth = System.currentTimeMillis()
            ))
        }
        while (kitsData.size > bornAlive) {
            kitsData.removeAt(kitsData.size - 1)
        }
        
        // Update all kits if genCount changes due to sire change
        for (i in kitsData.indices) {
            kitsData[i] = kitsData[i].copy(
                genCount = calculatedGen,
                sireId = sireId,
                fatherId = sireId
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record New Litter") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dam: ${dam?.name ?: ""} (${dam?.earTattoo ?: ""})", fontWeight = FontWeight.Bold)
                
                // Sire Selection
                ExposedDropdownMenuBox(
                    expanded = expandedSire,
                    onExpandedChange = { expandedSire = !expandedSire }
                ) {
                    val sireName = breederBucks.find { it.id == sireId }?.name ?: "Select Sire"
                    OutlinedTextField(
                        value = sireName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sire (Breeder Bucks)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSire) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedSire, onDismissRequest = { expandedSire = false }) {
                        breederBucks.forEach { buck ->
                            DropdownMenuItem(text = { Text(buck.name) }, onClick = { sireId = buck.id; expandedSire = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LitterIntSpinner("Born Alive", bornAliveStr, options09, Modifier.weight(1f)) { bornAliveStr = it }
                    LitterIntSpinner("Stillborn", stillbornStr, options09, Modifier.weight(1f)) { stillbornStr = it }
                }

                LitterIntSpinner("Mother Care Score (0-9)", careScore, options09, Modifier.fillMaxWidth()) { careScore = it }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Litter Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (kitsData.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Kit Details", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                    
                    kitsData.forEachIndexed { index, kit ->
                        KitEntryCard(
                            index = index,
                            kit = kit,
                            onUpdate = { updatedKit -> kitsData[index] = updatedKit }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val randomId = (1000..9999).random().toString()
                    onConfirm(
                        Litter(
                            litterId = randomId,
                            damId = dam?.id ?: "",
                            damName = dam?.name ?: "",
                            sireId = sireId,
                            bornAlive = bornAlive,
                            stillborn = stillbornStr.toIntOrNull() ?: 0,
                            kitCount = bornAlive + (stillbornStr.toIntOrNull() ?: 0),
                            mothersCareScore = careScore.toIntOrNull() ?: 0,
                            notes = notes
                        ),
                        kitsData.toList()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015))
            ) { Text("Record & Create Kits") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitEntryCard(index: Int, kit: Rabbit, onUpdate: (Rabbit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Kit #${index + 1} (G${kit.genCount})", fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = kit.name,
                onValueChange = { onUpdate(kit.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = kit.earTattoo,
                onValueChange = { onUpdate(kit.copy(earTattoo = it)) },
                label = { Text("Ear Tattoo") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = kit.color,
                    onValueChange = { onUpdate(kit.copy(color = it)) },
                    label = { Text("Color") },
                    modifier = Modifier.weight(1f)
                )
                
                var expandedSex by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSex,
                    onExpandedChange = { expandedSex = !expandedSex },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = kit.sex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sex") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSex) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedSex, onDismissRequest = { expandedSex = false }) {
                        listOf("Buck", "Doe").forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = { onUpdate(kit.copy(sex = s)); expandedSex = false })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitterIntSpinner(label: String, value: String, options: List<String>, modifier: Modifier, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onValueChange(opt); expanded = false })
            }
        }
    }
}
