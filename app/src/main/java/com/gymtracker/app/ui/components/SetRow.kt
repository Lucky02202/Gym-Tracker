package com.gymtracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymtracker.app.data.entity.WorkoutSet

/**
 * A single fast-entry row for a set: number, weight, reps, optional RIR, and a delete action.
 * Values are edited inline and committed via [onSetChanged] on every keystroke so the ViewModel
 * always has the latest values.
 */
@Composable
fun SetRow(
    set: WorkoutSet,
    units: String,
    onSetChanged: (WorkoutSet) -> Unit,
    onDelete: (WorkoutSet) -> Unit,
    modifier: Modifier = Modifier
) {
    var weightText by remember(set.id) { mutableStateOf(if (set.weight == 0.0) "" else formatNumber(set.weight)) }
    var repsText by remember(set.id) { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }
    var rirText by remember(set.id) { mutableStateOf(set.rir?.toString() ?: "") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${set.setNumber}",
            modifier = Modifier.width(24.dp),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = weightText,
            onValueChange = { value ->
                weightText = value
                val weight = value.toDoubleOrNull() ?: 0.0
                onSetChanged(set.copy(weight = weight))
            },
            label = { Text(units) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = repsText,
            onValueChange = { value ->
                repsText = value
                val reps = value.toIntOrNull() ?: 0
                onSetChanged(set.copy(reps = reps))
            },
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = rirText,
            onValueChange = { value ->
                rirText = value
                onSetChanged(set.copy(rir = value.toIntOrNull()))
            },
            label = { Text("RIR") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(0.7f)
        )

        IconButton(onClick = { onDelete(set) }) {
            Icon(Icons.Default.Close, contentDescription = "Delete set")
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
