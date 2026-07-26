package com.gymtracker.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.entity.WorkoutSummary
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.ui.viewmodel.CalendarViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onOpenWorkout: (Long) -> Unit
) {
    val viewModel: CalendarViewModel = viewModel(factory = factory)
    val summaries by viewModel.summaries.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val summariesByDate = remember(summaries) {
        summaries.groupBy {
            java.time.Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                }
            }

            val firstOfMonth = currentMonth.atDay(1)
            val leadingBlanks = firstOfMonth.dayOfWeek.value - 1 // Monday = 1
            val daysInMonth = currentMonth.lengthOfMonth()
            val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..daysInMonth).map { currentMonth.atDay(it) }

            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.weight(1f)) {
                items(cells) { date ->
                    if (date == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val hasWorkout = summariesByDate.containsKey(date)
                        val isToday = date == LocalDate.now()
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasWorkout -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    }
                                )
                                .clickable { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${date.dayOfMonth}",
                                color = if (hasWorkout) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            selectedDate?.let { date ->
                val dayWorkouts = summariesByDate[date].orEmpty()
                if (dayWorkouts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    dayWorkouts.forEach { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenWorkout(summary.id) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(summary.dayName, fontWeight = FontWeight.SemiBold)
                                Text("${summary.exerciseCount} exercises · ${summary.totalSets} sets", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
