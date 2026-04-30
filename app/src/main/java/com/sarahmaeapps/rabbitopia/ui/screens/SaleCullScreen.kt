package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.model.CullRecord
import com.sarahmaeapps.rabbitopia.model.Sale
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleCullScreen(
    rabbitId: String, 
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = viewModel()
) {
    var isCull by remember { mutableStateOf(false) }
    var reasonForCull by remember { mutableStateOf("") }
    var processedBy by remember { mutableStateOf("") }
    var processedFor by remember { mutableStateOf("") }
    
    var salePrice by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val customers by viewModel.customers.collectAsState()
    var customerExpanded by remember { mutableStateOf(false) }

    val currentDate = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date()) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (isCull) "Record Cull" else "Record Sale") },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCull) "Cull" else "Sale",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Switch(
                    checked = isCull,
                    onCheckedChange = { isCull = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }

            Text("Date: $currentDate", fontWeight = FontWeight.Medium)

            if (isCull) {
                OutlinedTextField(
                    value = reasonForCull,
                    onValueChange = { reasonForCull = it },
                    label = { Text("Reason for cull") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = processedBy,
                    onValueChange = { processedBy = it },
                    label = { Text("Processed by") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = processedFor,
                    onValueChange = { processedFor = it },
                    label = { Text("Processed for") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = !customerExpanded }
                ) {
                    val customerName = customers.find { it.id == customerId }?.name ?: "Select Customer"
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.name) },
                                onClick = {
                                    customerId = customer.id
                                    customerExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it },
                    label = { Text("Purchase Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Button(
                onClick = { 
                    if (isCull) {
                        viewModel.addCullRecord(CullRecord(
                            rabbitId = rabbitId,
                            reason = reasonForCull,
                            processedBy = processedBy,
                            processedFor = processedFor
                        ))
                    } else {
                        viewModel.addSale(Sale(
                            amount = salePrice.toDoubleOrNull() ?: 0.0,
                            customerId = customerId,
                            rabbitId = rabbitId
                        ))
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015))
            ) {
                Text("Confirm ${if (isCull) "Cull" else "Sale"}")
            }
        }
    }
}
