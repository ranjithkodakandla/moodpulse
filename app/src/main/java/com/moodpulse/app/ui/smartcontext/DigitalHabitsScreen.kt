package com.moodpulse.app.ui.smartcontext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.smartcontext.AppCategory
import com.moodpulse.app.smartcontext.SmartContextUiState
import com.moodpulse.app.smartcontext.SmartContextViewModel
import com.moodpulse.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalHabitsScreen(
    uiState: SmartContextUiState,
    viewModel: SmartContextViewModel,
    onNavigateToOnboarding: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Digital Habits",
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
            item {
                Text(
                    text = "LAST 7 DAYS",
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
                                text = "Smart Context tracks category usage on-device. Check back this evening for initial charts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLight.copy(alpha = 0.45f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
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
                        text = "RECENT OBSERVATIONS",
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
                TextButton(
                    onClick = { viewModel.setMonitoringEnabled(false) }
                ) {
                    Text(
                        "Turn off Smart Context",
                        color = MoodVeryLow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun CategoryBar(
    category: String,
    minutes: Int,
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val barColor = categoryColor(category)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextLight
            )
            Text(
                text = formatMinutes(minutes),
                fontSize = 12.sp,
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
                    .fillMaxWidth(fraction)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun ObservationRow(observationType: String) {
    val parts = observationType.split("_", limit = 2)
    val direction = if (parts[0] == "HIGH") "↑ More" else "↓ Less"
    val cat = parts.getOrElse(1) { "app" }.lowercase().replaceFirstChar { it.uppercase() }

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
                text = "$direction $cat usage than usual",
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
