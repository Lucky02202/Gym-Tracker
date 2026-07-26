package com.gymtracker.app.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.entity.WorkoutSummary
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.ui.viewmodel.HistoryViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    factory: ViewModelFactory,
    units: String,
    onOpenWorkout: (Long) -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(factory = factory)
    val query by viewModel.query.collectAsState()
    val summaries by viewModel.filteredSummaries.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("History", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setQuery(it) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Filter by day name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (summaries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workouts logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(summaries, key = { it.id }) { summary ->
                        WorkoutSummaryCard(summary, units, onClick = { onOpenWorkout(summary.id) })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkoutSummaryCard(summary: WorkoutSummary, units: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(summary.dayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(DateUtils.formatDate(summary.date), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            val duration = summary.endTime?.let { DateUtils.formatDuration(it - summary.startTime) } ?: "In progress"
            Text(
                "$duration · ${summary.exerciseCount} exercises · ${summary.totalSets} sets · ${summary.totalVolume.roundToInt()} $units volume",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
