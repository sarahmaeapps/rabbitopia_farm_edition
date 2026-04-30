package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CulledRabbitsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: RabbitViewModel = viewModel()
) {
    val rabbits by viewModel.culledRabbits.collectAsState()
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Culled Rabbits", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(horizontal = 16.dp)
        ) {
            items(rabbits) { rabbit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToDetail(rabbit.id) },
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Name: ${rabbit.name}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                            Text(text = "Ear Tattoo: ${rabbit.earTattoo}", color = Color.White)
                            Text(text = "DOC: ${rabbit.dateOfCull?.let { sdf.format(Date(it)) } ?: "N/A"}", color = Color.White, fontWeight = FontWeight.Medium)
                            Text(text = "Reason: ${rabbit.cullReason ?: "N/A"}", color = Color.White.copy(alpha = 0.8f), maxLines = 1)
                        }
                        IconButton(onClick = { viewModel.deleteRabbit(rabbit.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            }
        }
    }
}
