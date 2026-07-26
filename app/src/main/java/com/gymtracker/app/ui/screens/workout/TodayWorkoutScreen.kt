package com.gymtracker.app.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.ui.viewmodel.HomeViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * The "Today's Workout" tab. If a workout is already in progress, jumps straight into
 * the active logging UI; otherwise shows today's planned day (or rest day) with a
 * Start Workout button, per spec.
 */
@Composable
fun TodayWorkoutScreen(
    factory: ViewModelFactory,
    units: String
) {
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (state.inProgressWorkout != null) {
        ActiveWorkoutScreen(factory = factory, units = units, dayName = state.inProgressWorkout!!.workout.dayName)
        return
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Today's Workout", fontWeight = FontWeight.Bold) }) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                !state.hasActiveSplit -> Text(
                    "Set up your training split in Settings to get started.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                state.isRestDay -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Today is your rest day.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                }
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today's Workout", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        state.todayDayName ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = {
                        scope.launch { homeViewModel.startTodayWorkout() }
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Workout")
                    }
                }
            }
        }
    }
}
