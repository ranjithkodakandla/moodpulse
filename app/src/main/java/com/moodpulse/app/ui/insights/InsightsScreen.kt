package com.moodpulse.app.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.domain.*
import com.moodpulse.app.ui.components.EmptyState
import com.moodpulse.app.ui.components.GlossyMoodOrb
import com.moodpulse.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(uiState: InsightsUiState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Weekly Pulse",
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
            val insights = uiState.currentWeek

            // ── 3 Stat Cards ──
            item {
                StatStrip(
                    totalAllTime = uiState.totalAllTime,
                    streakDays = insights?.streakDays ?: 0,
                    thisWeekCount = insights?.totalEntries ?: 0
                )
            }

            // ── Gemma AI Insight Card ──
            item {
                GemmaAiInsightCard(
                    observation = uiState.observation.ifBlank {
                        "Your mood check-ins are logged privately on-device. Continue tracking daily to reveal rich cognitive and emotional trends."
                    }
                )
            }

            if (insights == null || !insights.hasEnoughData) {
                item {
                    InsufficientDataCard(count = insights?.totalEntries ?: 0)
                }
            } else {
                // ── Donut Ring Chart (Bearable-inspired) ──
                item {
                    DonutRingChartCard(insights = insights)
                }

                // ── Time of Day Peak Grid ──
                item {
                    TimeOfDayCard(insights = insights)
                }

                // ── Weekday Activity Chart ──
                item {
                    WeekdayGridCard(insights = insights)
                }

                // ── Trend Card ──
                if (insights.moodTrend != MoodTrend.INSUFFICIENT_DATA && insights.prevWeekAverageMood != null) {
                    item {
                        TrendCard(insights = insights)
                    }
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun StatStrip(totalAllTime: Int, streakDays: Int, thisWeekCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("This week", thisWeekCount.toString(), "check-ins", Modifier.weight(1f))
        StatCard("All time", totalAllTime.toString(), "entries", Modifier.weight(1f))
        StatCard("Streak", streakDays.toString(), "days 🔥", Modifier.weight(1f), highlightColor = Color(0xFFFF8F00))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = TextLight
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
            .padding(11.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextLight.copy(alpha = 0.35f)
            )
            Text(
                text = value,
                fontFamily = HeadlineFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
            Text(
                text = unit,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextLight.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun GemmaAiInsightCard(observation: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x1F00C853), Color(0x0800C853))
                )
            )
            .border(1.dp, Color(0x3800C853), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0x3300C853)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✦", fontSize = 12.sp, color = EmeraldGlow)
                    }
                    Text(
                        text = "Gemma Insight",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x2600C853))
                        .border(1.dp, Color(0x4D00C853), RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ON-DEVICE AI",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGlow,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Text(
                text = observation,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextLight.copy(alpha = 0.85f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * Donut Ring Chart inspired by Bearable.
 */
@Composable
private fun DonutRingChartCard(insights: WeeklyInsights) {
    val total = insights.totalEntries.coerceAtLeast(1)
    val avgMood = insights.averageMood

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Mood breakdown",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight.copy(alpha = 0.85f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Donut Canvas
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val stroke = 10.dp.toPx()
                        val diameter = size.minDimension - stroke
                        val radius = diameter / 2f
                        val arcSize = Size(diameter, diameter)
                        val topLeft = Offset(stroke / 2f, stroke / 2f)

                        // Background ring
                        drawArc(
                            color = Color(0x14FFFFFF),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )

                        // Arcs for each mood from Great -> Very Low
                        var currentAngle = -90f
                        Mood.entries.reversed().forEach { mood ->
                            val count = insights.moodDistribution[mood] ?: 0
                            val sweep = (count.toFloat() / total) * 360f
                            if (sweep > 0f) {
                                drawArc(
                                    color = moodColor(mood.value),
                                    startAngle = currentAngle,
                                    sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                                currentAngle += sweep
                            }
                        }
                    }

                    // Center text: Average score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", avgMood),
                            fontFamily = HeadlineFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = "AVG",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight.copy(alpha = 0.35f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Mood.entries.reversed().forEach { mood ->
                        val count = insights.moodDistribution[mood] ?: 0
                        val pct = ((count.toFloat() / total) * 100).toInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GlossyMoodOrb(moodValue = mood.value, size = 18.dp)
                            Text(
                                text = mood.label,
                                fontSize = 11.sp,
                                color = TextLight.copy(alpha = 0.65f),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$pct%",
                                fontSize = 9.sp,
                                color = TextLight.copy(alpha = 0.3f),
                                modifier = Modifier.width(28.dp)
                            )
                        }
                    }
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
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Time of day",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight.copy(alpha = 0.85f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                times.forEach { (tod, avg) ->
                    val moodVal = avg?.toInt()?.coerceIn(1, 5) ?: 3
                    val isPeak = tod == insights.peakTimeOfDay
                    val emoji = when (tod) {
                        TimeOfDay.MORNING -> "🌅"
                        TimeOfDay.AFTERNOON -> "☀️"
                        TimeOfDay.EVENING -> "🌆"
                        TimeOfDay.NIGHT -> "🌙"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isPeak) Color(0x1A00C853) else Color(0x08FFFFFF))
                            .border(
                                width = if (isPeak) 1.dp else 0.5.dp,
                                color = if (isPeak) Color(0x4D00C853) else Color(0x14FFFFFF),
                                shape = RoundedCornerShape(11.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(emoji, fontSize = 16.sp)
                            GlossyMoodOrb(moodValue = moodVal, size = 18.dp)
                            Text(
                                text = tod.label,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPeak) EmeraldGlow else TextLight.copy(alpha = 0.35f)
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "This week",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight.copy(alpha = 0.85f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                                    if (count > 0) Color(0x2E00C853)
                                    else Color(0x08FFFFFF)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (count > 0) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGlow
                                )
                            }
                        }
                        Text(
                            text = day,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendCard(insights: WeeklyInsights) {
    val (emoji, headline) = when (insights.moodTrend) {
        MoodTrend.IMPROVING -> Pair("📈", "Improving vs last week")
        MoodTrend.DECLINING -> Pair("📉", "Slightly lower vs last week")
        MoodTrend.STABLE -> Pair("〰️", "Consistent with last week")
        MoodTrend.INSUFFICIENT_DATA -> return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1200C853))
            .border(1.dp, Color(0x2E00C853), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Column {
                Text(
                    text = headline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGlow
                )
                Text(
                    text = "Avg ${String.format("%.1f", insights.averageMood)} / 5.0",
                    fontSize = 10.sp,
                    color = TextLight.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun InsufficientDataCard(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📈", fontSize = 32.sp)
            Text(
                text = "Building your weekly pulse",
                style = MaterialTheme.typography.titleMedium,
                color = TextLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Log at least 3 entries to unlock complete AI patterns and charts ($count/3 logged).",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight.copy(alpha = 0.45f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
