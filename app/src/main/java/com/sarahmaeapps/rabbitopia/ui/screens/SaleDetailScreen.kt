package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sarahmaeapps.rabbitopia.model.Customer
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.model.Sale
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    saleId: String,
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = viewModel(),
    rabbitViewModel: RabbitViewModel = viewModel()
) {
    var sale by remember { mutableStateOf<Sale?>(null) }
    var customer by remember { mutableStateOf<Customer?>(null) }
    var rabbit by remember { mutableStateOf<Rabbit?>(null) }

    LaunchedEffect(saleId) {
        val s = viewModel.getSaleById(saleId)
        sale = s
        s?.let {
            customer = viewModel.getCustomerById(it.customerId)
            rabbit = rabbitViewModel.getRabbitById(it.rabbitId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Record") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteSale(saleId)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        sale?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Section
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Customer Info", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Text("Name: ${customer?.name ?: "Unknown"}")
                        Text("Address: ${customer?.address ?: "N/A"}")
                        Text("Phone: ${customer?.phone ?: "N/A"}")
                    }
                }

                // Sale Details
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sale Details", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                        Text("Price: $${s.amount}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(s.date))}")
                    }
                }

                // Animal Info
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = rabbit?.imagePath,
                            contentDescription = "Rabbit Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Text("Animal: ${rabbit?.name ?: "Unknown"} (${rabbit?.earTattoo ?: "No Tag"})", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Document Scan
                if (s.documentImageUrl != null) {
                    Text("Document Scan", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                    Card(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        AsyncImage(
                            model = s.documentImageUrl,
                            contentDescription = "Document Scan",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Pedigree (4th Gen)
                Text("Pedigree Tree (G1-G4)", fontWeight = FontWeight.Bold, color = Color(0xFF880015))
                Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    rabbit?.let { r ->
                        PedigreeTree(r, viewModel = rabbitViewModel)
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
