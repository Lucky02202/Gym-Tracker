package com.gymtracker.app.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import com.gymtracker.app.ui.viewmodel.WorkoutDetailViewModel

@Composable
fun WorkoutDetailScreen(
    factory: ViewModelFactory,
    units: String,
    workoutId: Long,
    onBack: () -> Unit,
    onDuplicated: (Long) -> Unit
) {
    val viewModel: WorkoutDetailViewModel = viewModel(factory = factory)
    val workout by viewModel.workout.collectAsState()

    LaunchedEffect(workoutId) { viewModel.load(workoutId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.workout?.dayName ?: "Workout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.duplicateAsNewWorkout(onDuplicated) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate workout")
                    }
                }
            )
        }
    ) { padding ->
        val w = workout ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(DateUtils.formatDate(w.workout.date), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val duration = w.workout.endTime?.let { DateUtils.formatDuration(it - w.workout.startTime) } ?: "—"
            Text("Duration: $duration", style = MaterialTheme.typography.bodyMedium)
            w.workout.notes?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: $it", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            w.exercises.sortedBy { it.workoutExercise.orderIndex }.forEach { exWithSets ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(exWithSets.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        exWithSets.sets.sortedBy { it.setNumber }.forEach { set ->
                            Text(
                                "Set ${set.setNumber}: ${formatWeight(set.weight)} $units × ${set.reps} reps" +
                                    (set.rir?.let { " (RIR $it)" } ?: ""),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        val totalVolume = exWithSets.sets.sumOf { it.weight * it.reps }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Volume: ${formatWeight(totalVolume)} $units",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatWeight(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else String.format("%.1f", value)
