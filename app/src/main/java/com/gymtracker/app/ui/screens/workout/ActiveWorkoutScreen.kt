@file:OptIn(ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.entity.Exercise
import com.gymtracker.app.data.entity.MuscleGroups
import com.gymtracker.app.data.entity.WorkoutExerciseWithSets
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.data.util.OneRepMaxCalculator
import com.gymtracker.app.ui.components.SetRow
import com.gymtracker.app.ui.viewmodel.ActiveWorkoutViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActiveWorkoutScreen(
    factory: ViewModelFactory,
    units: String,
    dayName: String
) {
    val viewModel: ActiveWorkoutViewModel = viewModel(factory = factory)
    val workout by viewModel.workout.collectAsState()
    val recentExercises by viewModel.recentExercises.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddExercise by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(dayName) {
        viewModel.startOrResume(dayName)
    }

    // Live elapsed timer
    LaunchedEffect(workout?.workout?.startTime) {
        val start = workout?.workout?.startTime ?: return@LaunchedEffect
        while (true) {
            viewModel.setElapsedFromStart(start)
            delay(1000)
        }
    }

    val elapsed by viewModel.elapsedSeconds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column {
                    Text(workout?.workout?.dayName ?: dayName, fontWeight = FontWeight.Bold)
                    Text(formatElapsed(elapsed), style = MaterialTheme.typography.bodyMedium)
                } },
                actions = {
                    TextButton(onClick = { showDiscardConfirm = true }) { Text("Discard") }
                    Button(onClick = { showFinishDialog = true }) { Text("Finish") }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddExercise = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Exercise")
            }
        }
    ) { padding ->
        val exercises = workout?.exercises.orEmpty().sortedBy { it.workoutExercise.orderIndex }

        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No exercises yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap \"Add Exercise\" to get started, or duplicate your last $dayName workout.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.prefillFromLastWorkout(dayName) }) {
                        Text("Duplicate previous workout")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(exercises, key = { it.workoutExercise.id }) { exWithSets ->
                    ExerciseCard(
                        exerciseWithSets = exWithSets,
                        units = units,
                        onAddSet = { viewModel.addSet(exWithSets) },
                        onSetChanged = { viewModel.updateSet(it) },
                        onDeleteSet = { viewModel.deleteSet(it) },
                        onRemoveExercise = { viewModel.removeExercise(exWithSets) }
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    if (showAddExercise) {
        AddExerciseSheet(
            recentExercises = recentExercises,
            allExercises = allExercises,
            onDismiss = { showAddExercise = false },
            onPickExercise = { viewModel.addExercise(it.id); showAddExercise = false },
            onQuickAdd = { name, group -> viewModel.quickAddExercise(name, group); showAddExercise = false }
        )
    }

    if (showFinishDialog) {
        FinishWorkoutDialog(
            onDismiss = { showFinishDialog = false },
            onConfirm = { notes ->
                scope.launch { viewModel.finishWorkout(notes) }
                showFinishDialog = false
            }
        )
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard workout?") },
            text = { Text("This will delete everything logged in this session.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.discardWorkout() }
                    showDiscardConfirm = false
                }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { showDiscardConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ExerciseCard(
    exerciseWithSets: WorkoutExerciseWithSets,
    units: String,
    onAddSet: () -> Unit,
    onSetChanged: (com.gymtracker.app.data.entity.WorkoutSet) -> Unit,
    onDeleteSet: (com.gymtracker.app.data.entity.WorkoutSet) -> Unit,
    onRemoveExercise: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(exerciseWithSets.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(exerciseWithSets.exercise.muscleGroup, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(Icons.Default.Close, contentDescription = "Remove exercise")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            exerciseWithSets.sets.sortedBy { it.setNumber }.forEach { set ->
                SetRow(
                    set = set,
                    units = units,
                    onSetChanged = onSetChanged,
                    onDelete = onDeleteSet,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            val bestSet = exerciseWithSets.sets.maxByOrNull { it.weight }
            if (bestSet != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Est. 1RM: ${OneRepMaxCalculator.estimateRounded(bestSet.weight, bestSet.reps)} $units",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onAddSet, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Set")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseSheet(
    recentExercises: List<Exercise>,
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPickExercise: (Exercise) -> Unit,
    onQuickAdd: (String, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showQuickAdd by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Add Exercise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search or create new") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            val filtered = (if (query.isBlank()) recentExercises.ifEmpty { allExercises } else allExercises.filter {
                it.name.contains(query, ignoreCase = true)
            })

            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(filtered, key = { it.id }) { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        supportingContent = { Text(exercise.muscleGroup) },
                        modifier = Modifier.clickable { onPickExercise(exercise) }
                    )
                }
                if (query.isNotBlank() && filtered.none { it.name.equals(query, ignoreCase = true) }) {
                    item {
                        ListItem(
                            headlineContent = { Text("Create \"$query\"") },
                            modifier = Modifier.clickable { showQuickAdd = true }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showQuickAdd) {
        QuickAddExerciseDialog(
            initialName = query,
            onDismiss = { showQuickAdd = false },
            onConfirm = { name, group -> onQuickAdd(name, group) }
        )
    }
}

@Composable
private fun QuickAddExerciseDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var group by remember { mutableStateOf(MuscleGroups.PRESETS.first()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Exercise") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise name") }, singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Muscle group") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        MuscleGroups.PRESETS.forEach { preset ->
                            DropdownMenuItem(text = { Text(preset) }, onClick = { group = preset; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim(), group) }, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FinishWorkoutDialog(onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish workout") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(notes.ifBlank { null }) }) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Finish")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep going") } }
    )
}

private fun formatElapsed(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
