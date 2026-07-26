package com.gymtracker.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.ui.viewmodel.SearchViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory

@Composable
fun SearchScreen(
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onOpenExercise: (Long) -> Unit,
    onOpenWorkout: (Long) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(factory = factory)
    val query by viewModel.query.collectAsState()
    val exercises by viewModel.matchingExercises.collectAsState()
    val workouts by viewModel.matchingWorkouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setQuery(it) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Search exercises or workout history") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (exercises.isNotEmpty()) {
                    item { Text("Exercises", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
                    items(exercises, key = { "ex-${it.id}" }) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = { Text(exercise.muscleGroup) },
                            modifier = Modifier.clickable { onOpenExercise(exercise.id) }
                        )
                    }
                }
                if (workouts.isNotEmpty()) {
                    item { Text("Workouts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
                    items(workouts, key = { "w-${it.id}" }) { workout ->
                        ListItem(
                            headlineContent = { Text(workout.dayName) },
                            supportingContent = { Text(DateUtils.formatDate(workout.date)) },
                            modifier = Modifier.clickable { onOpenWorkout(workout.id) }
                        )
                    }
                }
            }
        }
    }
}
