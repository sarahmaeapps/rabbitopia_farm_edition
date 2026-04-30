package com.sarahmaeapps.rabbitopia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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
import com.sarahmaeapps.rabbitopia.data.SOPRepository
import com.sarahmaeapps.rabbitopia.model.SOPRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SOPViewModel(
    private val repository: SOPRepository = SOPRepository(),
    private val rabbitId: String
) : ViewModel() {
    val evaluations: StateFlow<List<SOPRecord>> = repository.getEvaluationsForRabbit(rabbitId)
        .map { list -> list.sortedByDescending { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvaluation(record: SOPRecord) {
        viewModelScope.launch {
            try {
                repository.addEvaluation(record)
            } catch (e: Exception) {
                android.util.Log.e("SOPViewModel", "Failed to save evaluation", e)
            }
        }
    }

    fun deleteEvaluation(id: String) {
        viewModelScope.launch { repository.deleteEvaluation(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOPChecklistScreen(
    rabbitId: String, 
    onNavigateBack: () -> Unit
) {
    val viewModel: SOPViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SOPViewModel(rabbitId = rabbitId) as T
        }
    })
    
    val evaluations by viewModel.evaluations.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<SOPRecord?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (selectedRecord != null) "Evaluation Detail" else if (showHistory) "Evaluation History" else "Mini Rex SOP Eval") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedRecord != null) selectedRecord = null
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedRecord == null) {
                        IconButton(onClick = { showHistory = !showHistory }) {
                            Icon(
                                if (showHistory) Icons.Default.Add else Icons.Default.History, 
                                contentDescription = if (showHistory) "New Evaluation" else "History",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when {
            selectedRecord != null -> {
                SOPRecordDetail(
                    record = selectedRecord!!,
                    modifier = Modifier.padding(padding)
                )
            }
            showHistory -> {
                SOPHistoryList(
                    evaluations = evaluations,
                    modifier = Modifier.padding(padding),
                    onDelete = { viewModel.deleteEvaluation(it) },
                    onSelect = { selectedRecord = it }
                )
            }
            else -> {
                SOPNewEvaluation(
                    rabbitId = rabbitId,
                    modifier = Modifier.padding(padding),
                    onSave = { 
                        viewModel.addEvaluation(it)
                        showHistory = true
                    }
                )
            }
        }
    }
}

@Composable
fun SOPRecordDetail(record: SOPRecord, modifier: Modifier = Modifier) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Evaluation Date: ${sdf.format(Date(record.date))}", fontWeight = FontWeight.Bold)
                Text("Total Score: ${record.totalScore} / 100", fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Recommendation: ${record.recommendation}", fontWeight = FontWeight.Bold, color = if(record.totalScore >= 85) Color.Green else Color(0xFF880015))
            }
        }

        SOPScoreDetail("Body", record.bodyScore, 35)
        SOPScoreDetail("Head & Ears", record.headEarScore, 10)
        SOPScoreDetail("Fur", record.furScore, 35)
        SOPScoreDetail("Color", record.colorScore, 15)
        SOPScoreDetail("Condition", record.conditionScore, 5)
    }
}

@Composable
fun SOPScoreDetail(label: String, score: Int, max: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontWeight = FontWeight.Bold)
                Text("$score / $max")
            }
            LinearProgressIndicator(
                progress = { score.toFloat() / max.toFloat() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                color = if (score.toFloat() / max.toFloat() > 0.8) Color.Green else Color.White
            )
        }
    }
}

@Composable
fun SOPHistoryList(
    evaluations: List<SOPRecord>, 
    modifier: Modifier = Modifier,
    onDelete: (String) -> Unit,
    onSelect: (SOPRecord) -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    if (evaluations.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No past evaluations found.", color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(evaluations) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(record) },
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sdf.format(Date(record.date)), fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(onClick = { onDelete(record.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                        Text("Score: ${record.totalScore} / 100", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Recommendation: ${record.recommendation}", color = if(record.totalScore >= 85) Color.Green else Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SOPNewEvaluation(
    rabbitId: String, 
    modifier: Modifier = Modifier,
    onSave: (SOPRecord) -> Unit
) {
    var bodyScore by remember { mutableFloatStateOf(0f) }
    var headEarScore by remember { mutableFloatStateOf(0f) }
    var furScore by remember { mutableFloatStateOf(0f) }
    var colorScore by remember { mutableFloatStateOf(0f) }
    var conditionScore by remember { mutableFloatStateOf(0f) }
    
    val checks = remember { mutableStateListOf(*Array(20) { false }) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("General Type (45 pts)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        SOPCheckItem("Balanced, compact body", checks, 0)
        SOPCheckItem("Smooth topline (ear base → hip peak → round tail)", checks, 1)
        SOPCheckItem("Shoulders well-developed", checks, 2)
        SOPCheckItem("Midsection slightly wider/deeper than shoulders", checks, 3)
        SOPCheckItem("Hindquarters full and rounded", checks, 4)
        SOPCheckItem("Legs short, straight", checks, 5)
        
        Slider(
            value = bodyScore, 
            onValueChange = { bodyScore = it }, 
            valueRange = 0f..35f, 
            steps = 35,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Text("Body Score: ${bodyScore.toInt()} / 35", color = Color.White)

        Text("Head & Ears (10 pts)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        SOPCheckItem("Head full and proportional", checks, 6)
        SOPCheckItem("Eyes bold and bright", checks, 7)
        SOPCheckItem("Ears thick, short, erect (≤ 3.5\")", checks, 8)
        
        Slider(
            value = headEarScore, 
            onValueChange = { headEarScore = it }, 
            valueRange = 0f..10f, 
            steps = 10,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Text("Head & Ears Score: ${headEarScore.toInt()} / 10", color = Color.White)

        Text("Fur (35 pts)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        SOPCheckItem("Extremely dense, upright, plush", checks, 9)
        SOPCheckItem("Even length (~5/8\")", checks, 10)
        SOPCheckItem("Springy resistance", checks, 11)
        SOPCheckItem("No protruding guard hairs", checks, 12)
        
        Slider(
            value = furScore, 
            onValueChange = { furScore = it }, 
            valueRange = 0f..35f, 
            steps = 35,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Text("Fur Score: ${furScore.toInt()} / 35", color = Color.White)

        Text("Color (15 pts)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        SOPCheckItem("Matches variety standard", checks, 13)
        SOPCheckItem("No mismarks", checks, 14)
        
        Slider(
            value = colorScore, 
            onValueChange = { colorScore = it }, 
            valueRange = 0f..15f, 
            steps = 15,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Text("Color Score: ${colorScore.toInt()} / 15", color = Color.White)

        Text("Condition (5 pts)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        SOPCheckItem("Good flesh condition, health, vitallity", checks, 15)
        
        Slider(
            value = conditionScore, 
            onValueChange = { conditionScore = it }, 
            valueRange = 0f..5f, 
            steps = 5,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Text("Condition Score: ${conditionScore.toInt()} / 5", color = Color.White)

        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
        
        val total = bodyScore + headEarScore + furScore + colorScore + conditionScore
        val recommendation = when {
            total >= 85 -> "KEEP FOR SHOW/BREEDING"
            total >= 70 -> "SELL FOR SHOW"
            else -> "SELL AS PET"
        }

        Text("TOTAL SCORE: ${total.toInt()} / 100", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.White)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recommendation:", fontWeight = FontWeight.Bold, color = Color.White)
                Text(recommendation, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if(total >= 85) Color.Green else Color(0xFF880015))
            }
        }
        
        Button(
            onClick = { 
                onSave(SOPRecord(
                    rabbitId = rabbitId,
                    bodyScore = bodyScore.toInt(),
                    headEarScore = headEarScore.toInt(),
                    furScore = furScore.toInt(),
                    colorScore = colorScore.toInt(),
                    conditionScore = conditionScore.toInt(),
                    totalScore = total.toInt(),
                    recommendation = recommendation,
                    checkedItems = checks.toList()
                ))
            }, 
            modifier = Modifier.fillMaxWidth(), 
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF880015),
                contentColor = Color.White
            )
        ) {
            Text("Save Evaluation")
        }
    }
}

@Composable
fun SOPCheckItem(text: String, checks: MutableList<Boolean>, index: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { checks[index] = !checks[index] }) {
        Checkbox(
            checked = checks[index], 
            onCheckedChange = { checks[index] = it },
            colors = CheckboxDefaults.colors(checkedColor = Color.White, checkmarkColor = Color.Black, uncheckedColor = Color.White)
        )
        Text(text, color = Color.White)
    }
}
