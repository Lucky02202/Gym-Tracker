package com.gymtracker.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.ui.components.StatCard
import com.gymtracker.app.ui.viewmodel.HomeViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory

@Composable
fun HomeScreen(
    factory: ViewModelFactory,
    onNavigateCalendar: () -> Unit,
    onNavigateBodyWeight: () -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateExerciseList: () -> Unit,
    onNavigateStatistics: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gym Progress Tracker", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = "Total Workouts", value = "${state.totalWorkouts}", modifier = Modifier.weight(1f))
                StatCard(label = "Current Streak", value = "${state.currentStreak} days", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Quick Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(quickAccessItems(onNavigateCalendar, onNavigateBodyWeight, onNavigateSearch, onNavigateExerciseList, onNavigateStatistics)) { item ->
                    QuickAccessCard(item)
                }
            }
        }
    }
}

private data class QuickAccessItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val onClick: () -> Unit)

private fun quickAccessItems(
    onCalendar: () -> Unit,
    onBodyWeight: () -> Unit,
    onSearch: () -> Unit,
    onExerciseList: () -> Unit,
    onStatistics: () -> Unit
) = listOf(
    QuickAccessItem("Calendar", Icons.Default.CalendarMonth, onCalendar),
    QuickAccessItem("Body Weight", Icons.Default.MonitorWeight, onBodyWeight),
    QuickAccessItem("Search", Icons.Default.Search, onSearch),
    QuickAccessItem("Exercises", Icons.Default.List, onExerciseList),
    QuickAccessItem("Statistics", Icons.Default.BarChart, onStatistics)
)

@Composable
private fun QuickAccessCard(item: QuickAccessItem) {
    Card(onClick = item.onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(item.icon, contentDescription = item.label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
