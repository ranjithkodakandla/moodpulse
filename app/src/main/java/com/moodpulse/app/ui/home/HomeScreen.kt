package com.moodpulse.app.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.moodpulse.app.ui.theme.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.data.db.MoodSuggestion
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.EmptyState
import com.moodpulse.app.ui.components.MoodDot
import com.moodpulse.app.ui.components.SectionHeader
import com.moodpulse.app.ui.smartcontext.SuggestionCard
import com.moodpulse.app.ui.theme.moodColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLogMood: () -> Unit,
    onSettingsClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    pendingSuggestion: MoodSuggestion? = null,
    onSuggestionConfirm: (Long, Int, List<String>) -> Unit = { _, _, _ -> },
    onSuggestionDismiss: (Long) -> Unit = {}
) {
    val greeting = remember {
        val hour = java.time.LocalTime.now().hour
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌿", fontSize = 20.sp)
                        Text(
                            "MoodPulse",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            AppIcons.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onLogMood,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Mood") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting + streak
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "How are you feeling?",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (uiState.streakDays > 0) {
                        StreakBadge(days = uiState.streakDays)
                    }
                }
            }

            // Today's mood timeline
            item {
                SectionHeader(
                    title = "Today",
                    action = {
                        Text(
                            "${uiState.todayEntries.size} entries",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (uiState.todayEntries.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🌤",
                        message = "No check-ins yet today",
                        subMessage = "Tap the button below to log your first mood"
                    )
                }
            } else {
                items(uiState.todayEntries, key = { it.id }) { entry ->
                    MoodEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }

            // Observation card
            if (uiState.todayObservation != null) {
                item {
                    ObservationCard(text = uiState.todayObservation)
                }
            }

            // Smart Context suggestion card
            if (pendingSuggestion != null) {
                item {
                    SuggestionCard(
                        suggestion = pendingSuggestion,
                        onConfirm = { mood, feelings ->
                            onSuggestionConfirm(pendingSuggestion.id, mood, feelings)
                        },
                        onDismiss = onSuggestionDismiss
                    )
                }
            }

            // Recent history
            if (uiState.recentEntries.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader(title = "Recent")
                }
                items(
                    uiState.recentEntries.filter { entry ->
                        // exclude today's entries already shown
                        val entryDate = Instant.ofEpochMilli(entry.timestampMs)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        entryDate != java.time.LocalDate.now()
                    }.take(5),
                    key = { "recent-${it.id}" }
                ) { entry ->
                    MoodEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StreakBadge(days: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                AppIcons.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$days day${if (days != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEntryCard(
    entry: MoodEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mood = Mood.fromValueOrNull(entry.mood)
    val time = remember(entry.timestampMs) {
        Instant.ofEpochMilli(entry.timestampMs)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    }
    val date = remember(entry.timestampMs) {
        val entryDate = Instant.ofEpochMilli(entry.timestampMs)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val today = java.time.LocalDate.now()
        when (entryDate) {
            today -> null
            today.minusDays(1) -> "Yesterday"
            else -> entryDate.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mood colour strip
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(moodColor(entry.mood))
            )

            Text(
                text = mood?.emoji ?: "😐",
                fontSize = 22.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mood?.label ?: "Okay",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (entry.feelingTags.isNotEmpty()) {
                        Text(
                            text = entry.feelingTags.take(2).joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (date != null) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                if (entry.source == "widget") {
                    Text(
                        "widget",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun ObservationCard(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", fontSize = 16.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
