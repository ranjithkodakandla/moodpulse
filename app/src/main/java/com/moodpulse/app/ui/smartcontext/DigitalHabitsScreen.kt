package com.moodpulse.app.ui.smartcontext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodpulse.app.smartcontext.AppCategory
import com.moodpulse.app.smartcontext.SmartContextUiState
import com.moodpulse.app.smartcontext.SmartContextViewModel
import com.moodpulse.app.ui.theme.AppIcons

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
                title = { Text("Digital Habits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!uiState.prefs.monitoringEnabled) {
            DisabledPlaceholder(
                onEnable = {
                    if (uiState.hasPermission) viewModel.setMonitoringEnabled(true)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Last 7 days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.categoryTotals.isEmpty()) {
                item {
                    Text(
                        text = "No data collected yet. Usage is collected overnight.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                val sortedCategories = uiState.categoryTotals.entries
                    .sortedByDescending { it.value }
                val maxMinutes = sortedCategories.firstOrNull()?.value ?: 1

                items(sortedCategories) { (category, minutes) ->
                    CategoryBar(
                        category = category,
                        minutes = minutes,
                        fraction = minutes.toFloat() / maxMinutes
                    )
                }
            }

            if (uiState.recentObservations.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Recent pattern observations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
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
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatMinutes(minutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = categoryColor(category),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ObservationRow(observationType: String) {
    val parts = observationType.split("_", limit = 2)
    val direction = if (parts[0] == "HIGH") "↑ More" else "↓ Less"
    val cat = parts.getOrElse(1) { "app" }.lowercase().replaceFirstChar { it.uppercase() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$direction $cat usage than usual",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DisabledPlaceholder(onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Context is off",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enable it to see your digital usage patterns alongside your mood.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onEnable) {
            Text("Enable Smart Context")
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
    "SOCIAL" -> MaterialTheme.colorScheme.tertiary
    "SHOPPING" -> MaterialTheme.colorScheme.error
    "ENTERTAINMENT" -> MaterialTheme.colorScheme.primary
    "PRODUCTIVITY" -> MaterialTheme.colorScheme.secondary
    "FINANCE" -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.outline
}
