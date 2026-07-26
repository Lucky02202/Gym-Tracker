package com.gymtracker.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.ui.viewmodel.BackupEvent
import com.gymtracker.app.ui.viewmodel.SettingsViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory

@Composable
fun SettingsScreen(
    factory: ViewModelFactory,
    onEditSplit: () -> Unit,
    onOpenExerciseList: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val settings by viewModel.settings.collectAsState()
    val activeSplit by viewModel.activeSplit.collectAsState()
    val backupEvent by viewModel.backupEvent.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let { viewModel.exportBackup(context, it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.importBackup(context, it) }
    }

    LaunchedEffect(backupEvent) {
        when (val event = backupEvent) {
            is BackupEvent.Success -> {
                snackbarHostState.showSnackbar("Done. Restart the app for changes to fully apply.")
                viewModel.clearBackupEvent()
            }
            is BackupEvent.Error -> {
                snackbarHostState.showSnackbar("Error: ${event.message}")
                viewModel.clearBackupEvent()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader("Training")
            ListItem(
                headlineContent = { Text("Workout Split") },
                supportingContent = { Text(activeSplit?.split?.name ?: "Not set up") },
                modifier = Modifier.clickableRow(onEditSplit)
            )
            ListItem(
                headlineContent = { Text("Exercise Library") },
                supportingContent = { Text("Manage your custom exercises") },
                modifier = Modifier.clickableRow(onOpenExerciseList)
            )

            HorizontalDivider()
            SettingsSectionHeader("Preferences")
            ListItem(
                headlineContent = { Text("Units") },
                trailingContent = {
                    SingleChoiceSegmented(
                        options = listOf("kg", "lbs"),
                        selected = settings.units,
                        onSelect = { viewModel.setUnits(it) }
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Theme") },
                trailingContent = {
                    SingleChoiceSegmented(
                        options = listOf("system", "light", "dark"),
                        selected = settings.themeMode,
                        onSelect = { viewModel.setThemeMode(it) }
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Default Rest Timer") },
                supportingContent = { Text("${settings.defaultRestTimerSeconds} seconds") },
                trailingContent = {
                    Row {
                        TextButton(onClick = { viewModel.setDefaultRestTimer((settings.defaultRestTimerSeconds - 15).coerceAtLeast(15)) }) { Text("−") }
                        TextButton(onClick = { viewModel.setDefaultRestTimer(settings.defaultRestTimerSeconds + 15) }) { Text("+") }
                    }
                }
            )

            HorizontalDivider()
            SettingsSectionHeader("Backup")
            ListItem(
                headlineContent = { Text("Export Backup") },
                supportingContent = { Text("Save all your data to a local file") },
                modifier = Modifier.clickableRow { exportLauncher.launch("gym-tracker-backup.db") }
            )
            ListItem(
                headlineContent = { Text("Restore Backup") },
                supportingContent = { Text("Replace current data from a local file") },
                modifier = Modifier.clickableRow { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "All data is stored locally on this device. No account, login, or internet connection is required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SingleChoiceSegmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option.replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
