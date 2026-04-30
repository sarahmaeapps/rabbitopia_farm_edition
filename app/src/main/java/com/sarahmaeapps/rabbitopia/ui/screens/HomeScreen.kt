package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahmaeapps.rabbitopia.ui.theme.SunshineDream
import com.sarahmaeapps.rabbitopia.ui.viewmodel.AlertType
import com.sarahmaeapps.rabbitopia.ui.viewmodel.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToRabbits: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToHousing: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToMedical: () -> Unit,
    onNavigateToCulls: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rabbitopia!",
            fontSize = 56.sp,
            fontFamily = SunshineDream,
            color = Color(0xFF880015), // Deep Red for Logo
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Greeting / Alerts Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (alerts.isNotEmpty()) MaterialTheme.colorScheme.errorContainer 
                                 else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (alerts.isEmpty()) {
                    val greeting = when {
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12 -> "Good morning"
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18 -> "Good afternoon"
                        else -> "Good evening"
                    }
                    Text(
                        text = "$greeting, ${viewModel.userName}!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Everything looks great today. Keep up the excellent work!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Active Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    alerts.forEach { alert ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            val icon = when (alert.type) {
                                AlertType.DUE_DATE -> "📅"
                                AlertType.WEANING -> "🍼"
                                AlertType.TEMPERATURE -> "🌡️"
                                AlertType.MESSAGE -> "📩"
                            }
                            Text(text = "$icon ${alert.title}", fontWeight = FontWeight.Bold)
                        }
                        Text(text = alert.message, style = MaterialTheme.typography.bodySmall)
                        if (alert != alerts.last()) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // Grid of Buttons
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeButton("Rabbits", Modifier.weight(1f), onNavigateToRabbits)
                HomeButton("Feed", Modifier.weight(1f), onNavigateToFeed)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeButton("Customers", Modifier.weight(1f), onNavigateToCustomers)
                HomeButton("Housing", Modifier.weight(1f), onNavigateToHousing)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeButton("Sales & Records", Modifier.weight(1f), onNavigateToSales)
                HomeButton("Medical / Health", Modifier.weight(1f), onNavigateToMedical)
            }
            
            // Thin Culls Button
            Button(
                onClick = onNavigateToCulls,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880015))
            ) {
                Text("Culls", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HomeButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(100.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF880015), // Signature Red
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
