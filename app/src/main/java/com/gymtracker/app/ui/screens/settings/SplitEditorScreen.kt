package com.gymtracker.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.entity.WorkoutDay
import com.gymtracker.app.data.util.SplitGenerator
import com.gymtracker.app.ui.viewmodel.SettingsViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory

private val weekdayLabels = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@Composable
fun SplitEditorScreen(
    factory: ViewModelFactory,
    onBack: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val activeSplit by viewModel.activeSplit.collectAsState()
    val allSplits by viewModel.allSplits.collectAsState()
    var editingDay by remember { mutableStateOf<WorkoutDay?>(null) }
    var showFrequencyPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Split", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            activeSplit?.let { split ->
                Text(
                    split.split.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(split.days.sortedBy { it.dayIndex }, key = { it.id }) { day ->
                        ListItem(
                            headlineContent = { Text(day.name, fontWeight = if (day.isRestDay) FontWeight.Normal else FontWeight.SemiBold) },
                            supportingContent = { Text(weekdayLabels.getOrElse(day.dayIndex) { "" }) },
                            trailingContent = {
                                IconButton(onClick = { editingDay = day }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit day")
                                }
                            }
                        )
                    }
                }
            } ?: Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No split set up yet.")
            }

            Button(
                onClick = { showFrequencyPicker = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Change Training Frequency")
            }

            if (allSplits.size > 1) {
                Text("Other Splits", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp))
                allSplits.filter { it.split.id != activeSplit?.split?.id }.forEach { split ->
                    ListItem(
                        headlineContent = { Text(split.split.name) },
                        modifier = Modifier.clickable { viewModel.activateSplit(split.split.id) }
                    )
                }
            }
        }
    }

    editingDay?.let { day ->
        EditDayDialog(
            day = day,
            onDismiss = { editingDay = null },
            onRename = { newName -> viewModel.renameDay(day, newName); editingDay = null },
            onSetRest = { viewModel.setDayAsRest(day); editingDay = null }
        )
    }

    if (showFrequencyPicker) {
        FrequencyPickerDialog(
            onDismiss = { showFrequencyPicker = false },
            onSelect = { days -> viewModel.changeSplitFrequency(days); showFrequencyPicker = false }
        )
    }
}

@Composable
private fun EditDayDialog(day: WorkoutDay, onDismiss: () -> Unit, onRename: (String) -> Unit, onSetRest: () -> Unit) {
    var name by remember { mutableStateOf(day.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(weekdayLabels.getOrElse(day.dayIndex) { "Day" }) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Day name") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onSetRest) { Text("Mark as rest day") }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onRename(name.trim()) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FrequencyPickerDialog(onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Days per week") },
        text = {
            Column {
                SplitGenerator.SUPPORTED_FREQUENCIES.forEach { days ->
                    ListItem(
                        headlineContent = { Text("$days Days / Week") },
                        supportingContent = { Text(SplitGenerator.generate(days).joinToString(" · ")) },
                        modifier = Modifier.clickable { onSelect(days) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
