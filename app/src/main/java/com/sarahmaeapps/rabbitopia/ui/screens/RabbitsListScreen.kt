package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RabbitsListScreen(
    onNavigateToRabbitDetail: (String) -> Unit,
    onNavigateToAddRabbit: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RabbitViewModel = viewModel()
) {
    val rabbits by viewModel.rabbits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animal Registry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddRabbit) {
                Icon(Icons.Default.Add, contentDescription = "Add Rabbit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(rabbits) { rabbit ->
                RabbitListItem(rabbit = rabbit, onClick = { onNavigateToRabbitDetail(rabbit.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RabbitListItem(rabbit: Rabbit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rabbit.name.ifEmpty { "Unnamed Rabbit" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "ID: ${rabbit.earTattoo}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF880015))
                ) {
                    Text(
                        text = rabbit.sex.take(1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = rabbit.breed, fontSize = 12.sp, color = Color.Gray)
                Text(text = rabbit.color, fontSize = 12.sp, color = Color.Gray)
                Text(text = rabbit.status, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
