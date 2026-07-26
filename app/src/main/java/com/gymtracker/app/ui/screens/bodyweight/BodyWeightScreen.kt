package com.gymtracker.app.ui.screens.bodyweight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.ui.components.LineChart
import com.gymtracker.app.ui.viewmodel.BodyWeightViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory

@Composable
fun BodyWeightScreen(
    factory: ViewModelFactory,
    units: String,
    onBack: () -> Unit
) {
    val viewModel: BodyWeightViewModel = viewModel(factory = factory)
    val entries by viewModel.entries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Weight", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add entry") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val chronological = entries.sortedBy { it.date }
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weight Trend ($units)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LineChart(values = chronological.map { it.weight.toFloat() })
            }

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No entries yet. Tap + to log your weight.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries, key = { it.id }) { entry ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${entry.weight} $units", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(DateUtils.formatDate(entry.date), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    entry.bodyFatPercent?.let { Text("Body fat: $it%", style = MaterialTheme.typography.bodyMedium) }
                                }
                                IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete entry")
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBodyWeightDialog(
            units = units,
            onDismiss = { showAddDialog = false },
            onConfirm = { weight, bodyFat, notes ->
                viewModel.addEntry(DateUtils.startOfToday(), weight, bodyFat, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddBodyWeightDialog(
    units: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double?, String?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Body Weight") },
        text = {
            Column {
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight ($units)") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = bodyFat, onValueChange = { bodyFat = it }, label = { Text("Body Fat % (optional)") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: return@TextButton
                    onConfirm(w, bodyFat.toDoubleOrNull(), notes.ifBlank { null })
                },
                enabled = weight.toDoubleOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
