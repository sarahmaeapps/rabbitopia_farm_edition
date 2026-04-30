package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sarahmaeapps.rabbitopia.R
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.ui.theme.SunshineDream
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RabbitDetailScreen(
    rabbitId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToPedigree: (String) -> Unit,
    onNavigateToSOP: (String) -> Unit,
    onNavigateToSaleCull: (String) -> Unit,
    onNavigateToWeighIn: (String) -> Unit,
    onNavigateToBreeding: (String) -> Unit,
    onNavigateToLitters: (String) -> Unit,
    onNavigateToMedical: () -> Unit,
    viewModel: RabbitViewModel = viewModel()
) {
    val rabbit by viewModel.getRabbitFlow(rabbitId).collectAsState(initial = null)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Animal Registry", style = MaterialTheme.typography.labelSmall)
                        rabbit?.let { 
                            Text(
                                it.name, 
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.deleteRabbit(rabbitId)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                    IconButton(onClick = { onNavigateToEdit(rabbitId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        rabbit?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Area with Image and Basic Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Name: ${r.name}", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Breed: ${r.breed}", fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                        Text(text = "Ear Tattoo: ${r.earTattoo}", color = Color.White)
                        Text(text = "Sex: ${r.sex}", color = Color.White)
                        Text(text = "Color: ${r.color}", color = Color.White)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF880015)),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "G${r.genCount}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Rabbit Image Box
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (r.imagePath != null) {
                            AsyncImage(
                                model = r.imagePath,
                                contentDescription = "Rabbit Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Hutch and Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Hutch ID", style = MaterialTheme.typography.labelSmall)
                            Text(r.hutchId, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Status", style = MaterialTheme.typography.labelSmall)
                            Text(r.status, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                // For Sale Status Row
                if (r.forSale) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LISTED FOR SALE", fontWeight = FontWeight.Black, color = Color.Green)
                            Text("Price: $${r.salePrice}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Rabbitopia!",
                    fontFamily = SunshineDream,
                    fontSize = 40.sp,
                    color = Color(0xFF880015),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons 3x2 Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionBtn("SOP Checklist", Modifier.weight(1f)) { onNavigateToSOP(rabbitId) }
                        ActionBtn("Weigh-In", Modifier.weight(1f)) { onNavigateToWeighIn(rabbitId) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionBtn("Sale / Cull", Modifier.weight(1f)) { onNavigateToSaleCull(rabbitId) }
                        ActionBtn("Breeding", Modifier.weight(1f)) { onNavigateToBreeding(rabbitId) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionBtn("Medical", Modifier.weight(1f)) { onNavigateToMedical() }
                        ActionBtn("Pedigree", Modifier.weight(1f)) { onNavigateToPedigree(rabbitId) }
                    }
                }

                // New Thin Litters & Kits Button
                Button(
                    onClick = { onNavigateToLitters(rabbitId) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015))
                ) {
                    Text("Litters and Kits", fontWeight = FontWeight.Bold)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ActionBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp), // Squished from 60dp
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015)),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold) // Smaller font
    }
}
