package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import android.content.Intent
import com.sarahmaeapps.rabbitopia.model.Rabbit
import com.sarahmaeapps.rabbitopia.ui.viewmodel.RabbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedigreeScreen(
    rabbitId: String,
    onNavigateBack: () -> Unit,
    viewModel: RabbitViewModel = viewModel()
) {
    var rabbit by remember { mutableStateOf<Rabbit?>(null) }
    val context = LocalContext.current
    
    LaunchedEffect(rabbitId) {
        rabbit = viewModel.getRabbitById(rabbitId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Pedigree: ${rabbit?.name ?: ""}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        rabbit?.let { r ->
                            val shareText = "Pedigree for ${r.name}\n" +
                                    "Breed: ${r.breed}\n" +
                                    "Ear Tattoo: ${r.earTattoo}\n" +
                                    "Sex: ${r.sex}\n" +
                                    "Color: ${r.color}\n" +
                                    "Generation: G${r.genCount}\n" +
                                    "Status: ${r.status}\n\n" +
                                    "Shared from Rabbitopia!"

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Pedigree")
                            context.startActivity(shareIntent)
                        } ?: run {
                            Toast.makeText(context, "Loading rabbit data...", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        rabbit?.let { r ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
            ) {
                PedigreeTree(r, viewModel)
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PedigreeTree(rabbit: Rabbit, viewModel: RabbitViewModel) {
    Row(
        modifier = Modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Generation 0: The Rabbit
        PedigreeBox(rabbit)
        
        PedigreeBranch(rabbit.sireId, rabbit.damId, 1, viewModel)
    }
}

@Composable
fun PedigreeBranch(sireId: String?, damId: String?, level: Int, viewModel: RabbitViewModel) {
    if (level > 3) return // G1, G2, G3 (Great-Grandparents)

    var sire by remember { mutableStateOf<Rabbit?>(null) }
    var dam by remember { mutableStateOf<Rabbit?>(null) }

    LaunchedEffect(sireId) {
        sireId?.let { sire = viewModel.getRabbitById(it) }
    }
    LaunchedEffect(damId) {
        damId?.let { dam = viewModel.getRabbitById(it) }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Visual connector line
        Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color.White.copy(alpha = 0.5f)))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Father's side
            Row(verticalAlignment = Alignment.CenterVertically) {
                PedigreeBox(sire, "Father")
                sire?.let { PedigreeBranch(it.sireId, it.damId, level + 1, viewModel) }
                    ?: PedigreeBranch(null, null, level + 1, viewModel)
            }
            
            // Mother's side
            Row(verticalAlignment = Alignment.CenterVertically) {
                PedigreeBox(dam, "Mother")
                dam?.let { PedigreeBranch(it.sireId, it.damId, level + 1, viewModel) }
                    ?: PedigreeBranch(null, null, level + 1, viewModel)
            }
        }
    }
}

@Composable
fun PedigreeBox(rabbit: Rabbit?, label: String = "") {
    val sexColor = when (rabbit?.sex) {
        "Buck" -> Color(0xFF1565C0).copy(alpha = 0.8f)
        "Doe" -> Color(0xFFAD1457).copy(alpha = 0.8f)
        else -> Color.DarkGray.copy(alpha = 0.8f)
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = sexColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (label.isNotEmpty() && rabbit == null) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
            Text(
                text = rabbit?.name ?: "Unknown",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color.White
            )
            Text(
                text = rabbit?.earTattoo ?: "---",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            if (rabbit != null) {
                Text(
                    text = "G${rabbit.genCount}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Yellow
                )
            }
        }
    }
}
