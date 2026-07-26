package com.gymtracker.app.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.ui.components.LineChart
import com.gymtracker.app.ui.components.StatCard
import com.gymtracker.app.ui.theme.PrGold
import com.gymtracker.app.ui.viewmodel.ExerciseProgressViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import kotlin.math.roundToInt

@Composable
fun ExerciseProgressScreen(
    factory: ViewModelFactory,
    units: String,
    exerciseId: Long,
    onBack: () -> Unit
) {
    val viewModel: ExerciseProgressViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val exercise by viewModel.exercise.collectAsState()

    LaunchedEffect(exerciseId) { viewModel.load(exerciseId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Progress", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Best 1RM",
                    value = "${uiState.prOneRepMax?.roundToInt() ?: "—"} $units",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PrGold) }
                )
                StatCard(label = "Top Weight", value = "${uiState.prWeight?.roundToInt() ?: "—"} $units", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            StatCard(label = "Best Session Volume", value = "${uiState.prVolume?.roundToInt() ?: "—"} $units", modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Text("Estimated 1RM Progression", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LineChart(values = uiState.history.map { it.estimatedOneRepMax.toFloat() })

            Spacer(modifier = Modifier.height(24.dp))
            Text("Weight Progression", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LineChart(values = uiState.history.map { it.maxWeight.toFloat() })

            Spacer(modifier = Modifier.height(24.dp))
            Text("Volume Progression", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LineChart(values = uiState.history.map { it.volume.toFloat() })

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
