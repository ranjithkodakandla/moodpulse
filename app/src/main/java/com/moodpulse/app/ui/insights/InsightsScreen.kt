package com.moodpulse.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.domain.*
import com.moodpulse.app.ui.components.EmptyState
import com.moodpulse.app.ui.theme.moodColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(uiState: InsightsUiState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Pulse", style = MaterialTheme.typography.titleLarge) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val insights = uiState.currentWeek

            item {
                // All-time stat strip
                StatStrip(
                    totalAllTime = uiState.totalAllTime,
                    streakDays = insights?.streakDays ?: 0,
                    thisWeekCount = insights?.totalEntries ?: 0
                )
            }

            if (insights == null || !insights.hasEnoughData) {
                item {
                    InsufficientDataCard(
                        count = insights?.totalEntries ?: 0,
                        message = uiState.observation
                    )
                }
            } else {
                item {
                    ObservationCard(text = uiState.observation)
                }

                item {
                    MoodDistributionCard(insights = insights)
                }

                item {
                    TimeOfDayCard(insights = insights)
                }

                item {
                    WeekdayGridCard(insights = insights)
                }

                if (insights.moodTrend != MoodTrend.INSUFFICIENT_DATA && insights.prevWeekAverageMood != null) {
                    item {
                        TrendCard(insights = insights)
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatStrip(totalAllTime: Int, streakDays: Int, thisWeekCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("This week", thisWeekCount.toString(), "check-ins", Modifier.weight(1f))
        StatCard("All time", totalAllTime.toString(), "entries", Modifier.weight(1f))
        StatCard("Streak", streakDays.toString(), "days", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InsufficientDataCard(count: Int, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📈", fontSize = 36.sp)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count < 3) {
                LinearProgressIndicator(
                    progress = { count / 3f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    "$count / 3 minimum entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ObservationCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", fontSize = 18.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun MoodDistributionCard(insights: WeeklyInsights) {
    val total = insights.totalEntries.coerceAtLeast(1)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Mood breakdown", style = MaterialTheme.typography.titleSmall)
            Mood.entries.reversed().forEach { mood ->
                val count = insights.moodDistribution[mood] ?: 0
                val fraction = count.toFloat() / total
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(mood.emoji, fontSize = 16.sp, modifier = Modifier.width(24.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(CircleShape)
                                .background(moodColor(mood.value))
                        )
                    }
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeOfDayCard(insights: WeeklyInsights) {
    val times = listOf(
        TimeOfDay.MORNING to insights.morningAvg,
        TimeOfDay.AFTERNOON to insights.afternoonAvg,
        TimeOfDay.EVENING to insights.eveningAvg,
        TimeOfDay.NIGHT to insights.nightAvg
    ).filter { it.second != null }

    if (times.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Time of day", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                times.forEach { (tod, avg) ->
                    val moodVal = avg!!.toInt().coerceIn(1, 5)
                    val isPeak = tod == insights.peakTimeOfDay
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPeak)
                                moodColor(moodVal).copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val todEmoji = when (tod) {
                                TimeOfDay.MORNING -> "🌅"
                                TimeOfDay.AFTERNOON -> "☀️"
                                TimeOfDay.EVENING -> "🌆"
                                TimeOfDay.NIGHT -> "🌙"
                            }
                            Text(todEmoji, fontSize = 18.sp)
                            Text(
                                Mood.fromValueOrNull(moodVal)?.emoji ?: "😐",
                                fontSize = 14.sp
                            )
                            Text(
                                tod.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayGridCard(insights: WeeklyInsights) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This week", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                days.forEachIndexed { idx, day ->
                    val dayOfWeek = idx + 1
                    val count = insights.dailyEntries[dayOfWeek] ?: 0
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (count > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (count > 0) {
                                Text(
                                    count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            day,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendCard(insights: WeeklyInsights) {
    val (emoji, headline, color) = when (insights.moodTrend) {
        MoodTrend.IMPROVING -> Triple("📈", "Mood improving vs last week", MaterialTheme.colorScheme.primary)
        MoodTrend.DECLINING -> Triple("📉", "Mood lower vs last week", MaterialTheme.colorScheme.secondary)
        MoodTrend.STABLE -> Triple("〰️", "Consistent with last week", MaterialTheme.colorScheme.onSurface)
        MoodTrend.INSUFFICIENT_DATA -> return
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 20.sp)
            Column {
                Text(headline, style = MaterialTheme.typography.bodyMedium, color = color)
                val diff = insights.averageMood - (insights.prevWeekAverageMood ?: insights.averageMood)
                Text(
                    "Avg mood: ${"%.1f".format(insights.averageMood)} / 5.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
