package com.moodpulse.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.EmptyState
import com.moodpulse.app.ui.home.MoodEntryCard
import com.moodpulse.app.ui.theme.moodColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onDateSelected: (LocalDate) -> Unit,
    onEntryClick: (Long) -> Unit
) {
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MoodCalendar(
                    displayMonth = displayMonth,
                    selectedDate = uiState.selectedDate,
                    datesWithEntries = uiState.allDatesWithEntries,
                    entriesByDate = uiState.entriesByDate,
                    onDateSelected = onDateSelected,
                    onMonthChange = { displayMonth = it }
                )
            }

            item {
                Text(
                    text = formatSelectedDate(uiState.selectedDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (uiState.selectedDateEntries.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "📅",
                        message = "No entries on this day",
                        subMessage = "Tap a date with a dot to see entries"
                    )
                }
            } else {
                items(uiState.selectedDateEntries, key = { it.id }) { entry ->
                    MoodEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MoodCalendar(
    displayMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithEntries: Set<LocalDate>,
    entriesByDate: Map<LocalDate, List<MoodEntry>>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(displayMonth.minusMonths(1)) }) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = {
                    if (displayMonth < YearMonth.now()) onMonthChange(displayMonth.plusMonths(1))
                }) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = "Next month",
                        tint = if (displayMonth < YearMonth.now())
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day of week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Calendar grid
            val firstDayOfMonth = displayMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // Mon=1
            val daysInMonth = displayMonth.lengthOfMonth()
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val rows = (totalCells + 6) / 7

            for (week in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dow in 0 until 7) {
                        val cellIndex = week * 7 + dow
                        val dayOfMonth = cellIndex - (firstDayOfWeek - 1) + 1
                        if (dayOfMonth < 1 || dayOfMonth > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = displayMonth.atDay(dayOfMonth)
                            val hasEntries = date in datesWithEntries
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val isFuture = date.isAfter(LocalDate.now())
                            val avgMood = if (hasEntries) {
                                entriesByDate[date]?.map { it.mood }?.average()?.toInt()
                            } else null

                            CalendarDay(
                                day = dayOfMonth,
                                isSelected = isSelected,
                                isToday = isToday,
                                isFuture = isFuture,
                                avgMoodValue = avgMood,
                                modifier = Modifier.weight(1f),
                                onClick = if (!isFuture) ({ onDateSelected(date) }) else ({})
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    avgMoodValue: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                else if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                else Modifier
            )
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontSize = 12.sp
            )
            if (avgMoodValue != null) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            else moodColor(avgMoodValue),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

private fun formatSelectedDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today · ${date.format(DateTimeFormatter.ofPattern("MMMM d"))}"
        today.minusDays(1) -> "Yesterday · ${date.format(DateTimeFormatter.ofPattern("MMMM d"))}"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }
}
