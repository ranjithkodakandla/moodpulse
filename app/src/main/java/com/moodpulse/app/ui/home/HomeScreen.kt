package com.moodpulse.app.ui.home

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.data.db.MoodSuggestion
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.*
import com.moodpulse.app.ui.smartcontext.SuggestionCard
import com.moodpulse.app.ui.theme.*
import java.time.Instant
import java.time.LocalDate
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
    onSuggestionCorrect: (Long, Int, List<String>, String?) -> Unit = { _, _, _, _ -> },
    onSuggestionAcknowledge: (Long, Boolean, String?) -> Unit = { _, _, _ -> },
    onSuggestionDismiss: (Long) -> Unit = {},
    onSuggestionSnooze: (Long) -> Unit = {}
) {
    val todayEntries = uiState.todayEntries
    val latestEntry = todayEntries.firstOrNull()
    val averageToday = remember(todayEntries) {
        if (todayEntries.isEmpty()) 0.0
        else todayEntries.map { it.mood }.average()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🌿 MoodPulse",
                            style = MaterialTheme.typography.titleLarge,
                            color = MintAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AA8D5A2))
                            .clickable(onClick = onSettingsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = "Settings",
                            tint = MintAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = EmeraldGlow,
                        spotColor = EmeraldGlow
                    )
                    .clip(RoundedCornerShape(50))
                    .background(EmeraldGlow)
                    .clickable(onClick = onLogMood)
                    .padding(horizontal = 22.dp, vertical = 13.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF003D14))
                    Text(
                        text = "Log Mood",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF003D14),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
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
            // ── Today Timeline Strip (Daylio-inspired) ──
            item {
                TodayTimelineStrip(
                    entries = todayEntries,
                    avgToday = averageToday,
                    streakDays = uiState.streakDays
                )
            }

            // ── Glowing Mood Ring Hero ──
            item {
                GlowingMoodRingHero(
                    latestEntry = latestEntry,
                    todayCount = todayEntries.size,
                    streakDays = uiState.streakDays
                )
            }

            // ── Smart Context Pending Suggestion (Mindful Pause) ──
            if (pendingSuggestion != null) {
                item {
                    SuggestionCard(
                        suggestion = pendingSuggestion,
                        onConfirm = onSuggestionConfirm,
                        onCorrect = onSuggestionCorrect,
                        onAcknowledge = onSuggestionAcknowledge,
                        onDismiss = onSuggestionDismiss,
                        onSnooze = onSuggestionSnooze
                    )
                }
            }

            // ── Today check-ins section ──
            item {
                SectionHeader(
                    title = "Today",
                    action = {
                        Text(
                            "${todayEntries.size} entries",
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.35f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }

            if (todayEntries.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🌤",
                        message = "No check-ins yet today",
                        subMessage = "Tap \"Log Mood\" below to capture how you are feeling."
                    )
                }
            } else {
                items(todayEntries, key = { it.id }) { entry ->
                    MoodEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }

            // ── Daily Observation Card ──
            if (!uiState.todayObservation.isNullOrBlank()) {
                item {
                    ObservationCard(text = uiState.todayObservation)
                }
            }

            // ── Recent past check-ins ──
            val recentPast = uiState.recentEntries.filter { entry ->
                todayEntries.none { it.id == entry.id }
            }
            if (recentPast.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader(title = "Recent")
                }
                items(recentPast.take(5), key = { it.id }) { entry ->
                    MoodEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
        }
    }
}

/**
 * Compact horizontal timeline strip showing today's logged orbs, avg score, and streak.
 */
@Composable
private fun TodayTimelineStrip(
    entries: List<MoodEntry>,
    avgToday: Double,
    streakDays: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (entries.isEmpty()) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🌤", fontSize = 16.sp)
                    Text(
                        text = "Ready for your first check-in",
                        fontSize = 12.sp,
                        color = TextLight.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Today's entry orbs + timestamps
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    entries.take(4).reversed().forEach { entry ->
                        val timeStr = remember(entry.timestampMs) {
                            val instant = Instant.ofEpochMilli(entry.timestampMs)
                            val zone = runCatching { ZoneId.of(entry.timezoneId) }.getOrDefault(ZoneId.systemDefault())
                            instant.atZone(zone).format(DateTimeFormatter.ofPattern("h:mm"))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            GlossyMoodOrb(moodValue = entry.mood, size = 26.dp)
                            Text(
                                text = timeStr,
                                fontSize = 9.sp,
                                color = TextLight.copy(alpha = 0.4f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Vertical separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color(0x1FFFFFFF))
                )

                // Today average score
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.1f", avgToday),
                        fontFamily = HeadlineFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow
                    )
                    Text(
                        text = "avg today",
                        fontSize = 8.sp,
                        color = TextLight.copy(alpha = 0.35f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Streak column
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color(0x1FFFFFFF))
            )

            Row(
                modifier = Modifier.padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🔥", fontSize = 14.sp)
                Text(
                    text = "$streakDays ${if (streakDays == 1) "day" else "days"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8F00)
                )
            }
        }
    }
}

/**
 * Large glowing central ring hero with glossy 3D mood orb.
 */
@Composable
private fun GlowingMoodRingHero(
    latestEntry: MoodEntry?,
    todayCount: Int,
    streakDays: Int
) {
    val mood = latestEntry?.let { Mood.fromValue(it.mood) } ?: Mood.GOOD
    val moodValue = latestEntry?.mood ?: 4
    val accentColor = moodColor(moodValue)

    val lastTime = remember(latestEntry?.timestampMs) {
        if (latestEntry == null) null
        else {
            val instant = Instant.ofEpochMilli(latestEntry.timestampMs)
            val zone = runCatching { ZoneId.of(latestEntry.timezoneId) }.getOrDefault(ZoneId.systemDefault())
            instant.atZone(zone).format(DateTimeFormatter.ofPattern("h:mm a"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Outer glowing ring
            Box(
                modifier = Modifier
                    .size(122.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                        )
                    )
                    .border(10.dp, accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Inner dark core
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurface)
                        .border(1.dp, Color(0x1FFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        GlossyMoodOrb(moodValue = moodValue, size = 42.dp)
                        Text(
                            text = mood.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Floating streak pill badge top-right
            if (streakDays > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 42.dp, y = (-42).dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFF8F00))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🔥 $streakDays",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Subtitle caption
        Text(
            text = if (todayCount > 0 && lastTime != null) {
                "$todayCount ${if (todayCount == 1) "entry" else "entries"} · last at $lastTime"
            } else {
                "Track how you feel throughout the day"
            },
            fontSize = 11.sp,
            color = TextLight.copy(alpha = 0.45f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Elegant observation card with soft green tint.
 */
@Composable
private fun ObservationCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x12A8D5A2))
            .border(1.dp, Color(0x22A8D5A2), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", fontSize = 15.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextLight.copy(alpha = 0.85f),
                lineHeight = 19.sp
            )
        }
    }
}
