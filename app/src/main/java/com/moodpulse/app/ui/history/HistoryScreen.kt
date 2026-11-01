package com.moodpulse.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.EmptyState
import com.moodpulse.app.ui.components.MoodEntryCard
import com.moodpulse.app.ui.components.SectionHeader
import com.moodpulse.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
                        style = MaterialTheme.typography.titleLarge,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        },
        containerColor = ObsidianBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Month Calendar ──
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

            // ── Year-in-Pixels (Pixels app-inspired 90-day grid) ──
            item {
                YearInPixelsCard(
                    entriesByDate = uiState.entriesByDate,
                    onDateSelected = onDateSelected
                )
            }

            // ── Selected Day Check-ins ──
            item {
                SectionHeader(
                    title = formatSelectedDate(uiState.selectedDate)
                )
            }

            if (uiState.selectedDateEntries.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "📅",
                        message = "No check-ins on this day",
                        subMessage = "Tap any date with a glowing dot to view check-ins"
                    )
                }
            } else {
                items(uiState.selectedDateEntries, key = { it.id }) { entry ->
                    MoodEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(displayMonth.minusMonths(1)) }) {
                    Icon(AppIcons.ChevronLeft, contentDescription = "Previous month", tint = TextLight.copy(alpha = 0.6f))
                }
                Text(
                    text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontFamily = HeadlineFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
                IconButton(onClick = {
                    if (displayMonth < YearMonth.now()) onMonthChange(displayMonth.plusMonths(1))
                }) {
                    Icon(
                        AppIcons.ChevronRight,
                        contentDescription = "Next month",
                        tint = if (displayMonth < YearMonth.now()) TextLight.copy(alpha = 0.6f) else TextLight.copy(alpha = 0.15f)
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight.copy(alpha = 0.28f)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

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

                            CalendarDayCell(
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
private fun CalendarDayCell(
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
            .padding(2.5.dp)
            .clip(CircleShape)
            .then(
                when {
                    isToday -> Modifier
                        .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = EmeraldGlow, spotColor = EmeraldGlow)
                        .background(EmeraldGlow)
                    isSelected -> Modifier
                        .background(Color(0x2E00C853))
                        .border(1.dp, EmeraldGlow, CircleShape)
                    else -> Modifier
                }
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
                fontSize = 11.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = when {
                    isToday -> Color(0xFF003D14)
                    isSelected -> EmeraldGlow
                    isFuture -> TextLight.copy(alpha = 0.2f)
                    else -> TextLight
                }
            )
            if (avgMoodValue != null && !isToday) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(moodColor(avgMoodValue))
                )
            }
        }
    }
}

/**
 * 90-Day "Year in Pixels" Grid (inspired by the Pixels mood tracking app).
 */
@Composable
private fun YearInPixelsCard(
    entriesByDate: Map<LocalDate, List<MoodEntry>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val daysCount = 91 // 13 columns * 7 rows = 91 days
    val dates = remember(today) {
        (0 until daysCount).map { today.minusDays((daysCount - 1 - it).toLong()) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header with legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "90 days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight.copy(alpha = 0.8f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(MoodVeryLow, MoodLow, MoodOkay, MoodGood, MoodGreat).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                    Text(
                        text = "V.Low → Great",
                        fontSize = 8.sp,
                        color = TextLight.copy(alpha = 0.35f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 13 columns x 7 rows pixel grid
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (row in 0 until 7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (col in 0 until 13) {
                            val index = col * 7 + row
                            if (index < dates.size) {
                                val date = dates[index]
                                val entries = entriesByDate[date]
                                val avgMood = entries?.map { it.mood }?.average()?.toInt()
                                val cellColor = if (avgMood != null) moodColor(avgMood) else Color(0x0EFFFFFF)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(2.5.dp))
                                        .background(cellColor)
                                        .clickable { onDateSelected(date) }
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
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
