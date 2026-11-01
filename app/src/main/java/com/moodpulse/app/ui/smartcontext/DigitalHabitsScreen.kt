package com.moodpulse.app.ui.smartcontext

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.smartcontext.AppCategory
import com.moodpulse.app.smartcontext.SmartContextUiState
import com.moodpulse.app.smartcontext.SmartContextViewModel
import com.moodpulse.app.ui.components.GlossyMoodOrb
import com.moodpulse.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalHabitsScreen(
    uiState: SmartContextUiState,
    viewModel: SmartContextViewModel,
    onNavigateToOnboarding: () -> Unit,
    onBack: () -> Unit,
    onLogMoodFromHabit: ((Mood, List<FeelingTag>, String) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Digital Habits & AI Pulse",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        },
        containerColor = ObsidianBg
    ) { padding ->
        if (!uiState.prefs.monitoringEnabled) {
            DisabledPlaceholder(
                onEnable = {
                    viewModel.refreshPermission()
                    if (viewModel.hasPermission()) viewModel.setMonitoringEnabled(true)
                    else onNavigateToOnboarding()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── AI Habit-to-Mood Translation Card ──
            if (uiState.categoryTotals.isNotEmpty()) {
                val topCategoryEntry = uiState.categoryTotals.maxByOrNull { it.value }
                val topCategory = topCategoryEntry?.key ?: "SOCIAL"
                val topMinutes = topCategoryEntry?.value ?: 0

                item {
                    AIHabitMoodCorrelationCard(
                        topCategory = topCategory,
                        topMinutes = topMinutes,
                        hasShopping = (uiState.categoryTotals["SHOPPING"] ?: 0) >= 10,
                        shoppingMinutes = uiState.categoryTotals["SHOPPING"] ?: 0,
                        hasSocial = (uiState.categoryTotals["SOCIAL"] ?: 0) >= 20,
                        socialMinutes = uiState.categoryTotals["SOCIAL"] ?: 0,
                        onLogCheckIn = { mood, tags, note ->
                            onLogMoodFromHabit?.invoke(mood, tags, note)
                        },
                        onTriggerHomeCheckIn = {
                            viewModel.generateSuggestionFromHabits(topCategory)
                        }
                    )
                }
            }

            item {
                Text(
                    text = "LAST 7 DAYS USAGE BREAKDOWN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = TextLight.copy(alpha = 0.35f)
                )
            }

            if (uiState.categoryTotals.isEmpty()) {
                item {
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
                            Text("📊", fontSize = 32.sp)
                            Text(
                                text = "Observing usage patterns",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Smart Context tracks category usage on-device. Use your phone naturally today and check back here for active pattern translation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLight.copy(alpha = 0.45f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.refreshHabits(forceGenerateSuggestion = true) },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow, contentColor = Color(0xFF003D14))
                            ) {
                                Text("✦ Refresh Habits Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                val maxMinutes = uiState.categoryTotals.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x0AFFFFFF))
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            uiState.categoryTotals.entries.sortedByDescending { it.value }.forEach { (category, minutes) ->
                                CategoryBar(
                                    category = category,
                                    minutes = minutes,
                                    fraction = minutes.toFloat() / maxMinutes
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.recentObservations.isNotEmpty()) {
                item {
                    Text(
                        text = "AI BEHAVIOURAL OBSERVATIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = TextLight.copy(alpha = 0.35f)
                    )
                }
                items(uiState.recentObservations.take(5)) { obs ->
                    ObservationRow(observationType = obs.observationType)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.refreshHabits(forceGenerateSuggestion = true) }
                    ) {
                        Text("✦ Rescan Habits & Correlate", color = MintAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { viewModel.setMonitoringEnabled(false) }
                    ) {
                        Text(
                            "Turn off",
                            color = MoodVeryLow.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
        }
    }
}

/**
 * High-impact card that directly translates digital screen habits into mood inferences and mindful pause warnings.
 */
@Composable
private fun AIHabitMoodCorrelationCard(
    topCategory: String,
    topMinutes: Int,
    hasShopping: Boolean,
    shoppingMinutes: Int,
    hasSocial: Boolean,
    socialMinutes: Int,
    onLogCheckIn: (Mood, List<FeelingTag>, String) -> Unit,
    onTriggerHomeCheckIn: () -> Unit
) {
    // Inferred state based on habits
    val (inferredMood, inferredTags, title, insightText, warningEmoji) = when {
        hasShopping -> {
            Tuple5(
                Mood.LOW,
                listOf(FeelingTag.ANXIOUS, FeelingTag.STRESSED),
                "SHOPPING & SPENDING PAUSE",
                "High shopping activity detected (${formatMinutes(shoppingMinutes)}). Spikes in retail browsing or scrolling e-commerce apps often signal an emotional escape or stress-spending. Take a pause before checking out.",
                "🛍"
            )
        }
        hasSocial -> {
            Tuple5(
                Mood.OKAY,
                listOf(FeelingTag.TIRED, FeelingTag.STRESSED),
                "SOCIAL MEDIA SCROLLING PAUSE",
                "Extensive social media scrolling detected (${formatMinutes(socialMinutes)} on Instagram/Facebook). Long feeds frequently correlate with cognitive fatigue, comparison anxiety, or restless mood.",
                "📱"
            )
        }
        topCategory == "ENTERTAINMENT" -> {
            Tuple5(
                Mood.OKAY,
                listOf(FeelingTag.CALM, FeelingTag.TIRED),
                "ENTERTAINMENT & MEDIA WIND-DOWN",
                "Substantial entertainment and video streaming detected (${formatMinutes(topMinutes)}). High passive media time often reflects a need to decompress after a demanding period.",
                "🎬"
            )
        }
        topCategory == "PRODUCTIVITY" -> {
            Tuple5(
                Mood.GOOD,
                listOf(FeelingTag.FOCUSED, FeelingTag.ENERGISED),
                "PRODUCTIVITY & FOCUS STREAK",
                "Strong focused screen time detected (${formatMinutes(topMinutes)}). Great momentum — remember to check in on your energy levels to prevent late-day burnout.",
                "💼"
            )
        }
        else -> {
            Tuple5(
                Mood.OKAY,
                listOf(FeelingTag.CALM),
                "DIGITAL HABIT TRANSLATION",
                "Active digital engagement detected (${formatMinutes(topMinutes)} in ${topCategory.lowercase()}). Pausing to name your mood keeps you mindful and grounded.",
                "✦"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x2400C853), Color(0x0C004F1A))
                )
            )
            .border(1.dp, Color(0x3800C853), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(warningEmoji, fontSize = 14.sp)
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = EmeraldGlow
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x2600C853))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "ON-DEVICE AI",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGlow
                    )
                }
            }

            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                lineHeight = 19.sp
            )

            // Direct Translation Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val note = "✦ Mindful Check-in based on $title ($topCategory)"
                        onLogCheckIn(inferredMood, inferredTags, note)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGlow,
                        contentColor = Color(0xFF003D14)
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GlossyMoodOrb(moodValue = inferredMood.value, size = 16.dp)
                        Text(
                            text = "Log ${inferredMood.label}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onTriggerHomeCheckIn,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MintAccent),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0x3800C853))
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = "Trigger Home Pause",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

@Composable
private fun CategoryBar(
    category: String,
    minutes: Int,
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val barColor = categoryColor(category)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
                Text(
                    text = category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextLight
                )
            }
            Text(
                text = formatMinutes(minutes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0x14FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0.04f, 1f))
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun ObservationRow(observationType: String) {
    val parts = observationType.split("_", limit = 2)
    val direction = if (parts[0] == "HIGH") "↑ High" else "↓ Low"
    val cat = parts.getOrElse(1) { "activity" }.lowercase().replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1200C853))
            .border(1.dp, Color(0x2600C853), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("💡", fontSize = 14.sp)
            Text(
                text = "$direction $cat activity · Correlating with mood patterns",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextLight.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun DisabledPlaceholder(onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Smart Context is off",
            fontFamily = HeadlineFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextLight
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enable it to understand your digital habits and receive gentle check-in suggestions before distraction spirals.",
            fontSize = 13.sp,
            color = TextLight.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onEnable,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldGlow,
                contentColor = Color(0xFF003D14)
            )
        ) {
            Text("Enable Smart Context", fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
private fun categoryColor(category: String) = when (category.uppercase()) {
    "SOCIAL" -> MoodOkay
    "SHOPPING" -> MoodLow
    "ENTERTAINMENT" -> MoodGood
    "PRODUCTIVITY" -> EmeraldGlow
    "FINANCE" -> Color(0xFFFFCA28)
    else -> MintAccent
}
