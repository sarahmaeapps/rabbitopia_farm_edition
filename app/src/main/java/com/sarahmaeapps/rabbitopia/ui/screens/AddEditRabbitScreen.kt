package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRabbitScreen(
    rabbitId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: RabbitViewModel = viewModel()
) {
    var earTattoo by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("Buck") }
    var color by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var genCount by remember { mutableStateOf("0") }
    var status by remember { mutableStateOf("Breeder") }
    var forSale by remember { mutableStateOf(false) }
    var salePrice by remember { mutableStateOf("") }
    var sireId by remember { mutableStateOf<String?>(null) }
    var damId by remember { mutableStateOf<String?>(null) }
    var cullReason by remember { mutableStateOf("") }
    var dob by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingImagePath by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dob)

    val statusOptions = listOf("Breeder", "Show", "4H Starter", "Meat", "Cull")
    var expandedStatus by remember { mutableStateOf(false) }

    val breederBucks by viewModel.breederBucks.collectAsState()
    val breederDoes by viewModel.breederDoes.collectAsState()
    var expandedSire by remember { mutableStateOf(false) }
    var expandedDam by remember { mutableStateOf(false) }
    
    val alphabet = ('A'..'Z').map { it.toString() }
    val numbers = (1..9).map { it.toString() }
    
    var selectedLetter by remember { mutableStateOf(alphabet[0]) }
    var selectedNumber by remember { mutableStateOf(numbers[0]) }
    var expandedLetter by remember { mutableStateOf(false) }
    var expandedNumber by remember { mutableStateOf(false) }
    
    var isSaving by remember { mutableStateOf(false) }
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedUri = it
    }

    // Auto-calculate Generation Count
    LaunchedEffect(sireId, damId) {
        val selectedSire = breederBucks.find { it.id == sireId }
        val selectedDam = breederDoes.find { it.id == damId }
        
        if (selectedSire != null && selectedDam != null) {
            val calculatedGen = minOf(selectedSire.genCount, selectedDam.genCount) + 1
            genCount = calculatedGen.toString()
        } else if (selectedSire != null) {
            genCount = (selectedSire.genCount + 1).toString()
        } else if (selectedDam != null) {
            genCount = (selectedDam.genCount + 1).toString()
        }
    }

    // Logic to load rabbit if rabbitId is not null could go here
    LaunchedEffect(rabbitId) {
        if (rabbitId != null) {
            val rabbit = viewModel.getRabbitById(rabbitId)
            rabbit?.let {
                earTattoo = it.earTattoo
                name = it.name
                breed = it.breed
                sex = it.sex
                color = it.color
                weight = it.weight.toString()
                genCount = it.genCount.toString()
                status = it.status
                forSale = it.forSale
                salePrice = it.salePrice.toString()
                sireId = it.sireId
                damId = it.damId
                dob = it.dateOfBirth ?: System.currentTimeMillis()
                existingImagePath = it.imagePath
                if (it.hutchId.length >= 2) {
                    val letter = it.hutchId.takeWhile { c -> c.isLetter() }
                    val number = it.hutchId.dropWhile { c -> c.isLetter() }.trim()
                    if (letter.isNotEmpty() && alphabet.contains(letter)) selectedLetter = letter
                    if (number.isNotEmpty() && numbers.contains(number)) selectedNumber = number
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (rabbitId == null) "Add Rabbit" else "Edit Rabbit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            OutlinedTextField(
                value = earTattoo,
                onValueChange = { earTattoo = it },
                label = { Text("Ear Tattoo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed (e.g. Mini Rex, Holland Lop)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text("Sex", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = sex == "Buck", 
                        onClick = { sex = "Buck" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.White.copy(alpha = 0.6f))
                    )
                    Text("Buck", color = Color.White)
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = sex == "Doe", 
                        onClick = { sex = "Doe" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.White.copy(alpha = 0.6f))
                    )
                    Text("Doe", color = Color.White)
                }
            }

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = genCount,
                onValueChange = { genCount = it },
                label = { Text("Generation Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                status = option
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            if (status == "Cull") {
                OutlinedTextField(
                    value = cullReason,
                    onValueChange = { cullReason = it },
                    label = { Text("Reason for Cull") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mother and Father Fields
            Text("Pedigree Info", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.White)
            
            ExposedDropdownMenuBox(
                expanded = expandedSire,
                onExpandedChange = { expandedSire = !expandedSire }
            ) {
                val sireName = breederBucks.find { it.id == sireId }?.name ?: "Select Father (Sire)"
                OutlinedTextField(
                    value = sireName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Father (Sire)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSire) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedSire, onDismissRequest = { expandedSire = false }) {
                    breederBucks.forEach { buck ->
                        DropdownMenuItem(text = { Text(buck.name) }, onClick = { sireId = buck.id; expandedSire = false })
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedDam,
                onExpandedChange = { expandedDam = !expandedDam }
            ) {
                val damName = breederDoes.find { it.id == damId }?.name ?: "Select Mother (Dam)"
                OutlinedTextField(
                    value = damName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mother (Dam)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDam) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedDam, onDismissRequest = { expandedDam = false }) {
                    breederDoes.forEach { doe ->
                        DropdownMenuItem(text = { Text(doe.name) }, onClick = { damId = doe.id; expandedDam = false })
                    }
                }
            }

            // Birthdate Field
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            OutlinedTextField(
                value = sdf.format(Date(dob)),
                onValueChange = { },
                readOnly = true,
                label = { Text("Birthdate") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Change")
                    }
                }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            dob = datePickerState.selectedDateMillis ?: dob
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Text("Hutch ID", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Letter Spinner
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
                    ExposedDropdownMenu(
                        expanded = expandedLetter,
                        onDismissRequest = { expandedLetter = false }
                    ) {
                        alphabet.forEach { letter ->
                            DropdownMenuItem(
                                text = { Text(letter) },
                                onClick = {
                                    selectedLetter = letter
                                    expandedLetter = false
                                }
                            )
                        }
                    }
                }

                // Number Spinner
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
                    ExposedDropdownMenu(
                        expanded = expandedNumber,
                        onDismissRequest = { expandedNumber = false }
                    ) {
                        numbers.forEach { number ->
                            DropdownMenuItem(
                                text = { Text(number) },
                                onClick = {
                                    selectedNumber = number
                                    expandedNumber = false
                                }
                            )
                        }
                    }
                }
            }

            // For Sale Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = forSale, onCheckedChange = { forSale = it })
                Text("List for Sale (Companion App)", color = Color.White)
            }

            if (forSale) {
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it },
                    label = { Text("Sale Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (selectedUri != null || existingImagePath != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        AsyncImage(
                            model = selectedUri ?: existingImagePath,
                            contentDescription = "Rabbit Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(if (selectedUri == null) "Upload Rabbit Photo" else "Change Photo ✅")
            }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true
                    val rabbit = Rabbit(
                        id = rabbitId ?: "",
                        earTattoo = earTattoo,
                        name = name,
                        breed = breed,
                        sex = sex,
                        color = color,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        genCount = genCount.toIntOrNull() ?: 0,
                        hutchId = "$selectedLetter $selectedNumber",
                        status = status,
                        forSale = forSale,
                        salePrice = salePrice.toDoubleOrNull() ?: 0.0,
                        sireId = sireId,
                        damId = damId,
                        fatherId = sireId,
                        motherId = damId,
                        imagePath = existingImagePath,
                        dateOfBirth = dob,
                        dateOfCull = if (status == "Cull") System.currentTimeMillis() else null,
                        cullReason = if (status == "Cull") cullReason else null
                    )
                    
                    viewModel.addRabbit(rabbit, selectedUri) {
                        isSaving = false
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF880015),
                    contentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Rabbit")
                }
            }
        }
    }
}
