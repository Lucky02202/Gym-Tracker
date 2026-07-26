package com.gymtracker.app.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.ui.components.StatCard
import com.gymtracker.app.ui.viewmodel.StatisticsViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    factory: ViewModelFactory,
    units: String,
    onBack: () -> Unit
) {
    val viewModel: StatisticsViewModel = viewModel(factory = factory)
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        val items = listOf(
            "Total Workouts" to "${stats.totalWorkouts}",
            "Current Streak" to "${stats.currentStreak} days",
            "Longest Streak" to "${stats.longestStreak} days",
            "Exercises Performed" to "${stats.exercisesPerformed}",
            "Total Sets" to "${stats.totalSets}",
            "Total Reps" to "${stats.totalReps}",
            "Total Weight Lifted" to "${stats.totalWeightLifted.roundToInt()} $units",
            "Avg. Workout Duration" to DateUtils.formatDuration(stats.averageWorkoutDurationMillis)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { (label, value) ->
                StatCard(label = label, value = value, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
