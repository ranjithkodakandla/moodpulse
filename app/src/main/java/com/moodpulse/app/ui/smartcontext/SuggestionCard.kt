package com.moodpulse.app.ui.smartcontext

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodpulse.app.data.db.MoodSuggestion

@Composable
fun SuggestionCard(
    suggestion: MoodSuggestion,
    onConfirm: (Int, List<String>) -> Unit,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showActions by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = { onDismiss(suggestion.id) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                text = suggestion.promptText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            if (!showActions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val feelings = suggestion.candidateFeelings
                                .split(",").filter { it.isNotBlank() }
                            onConfirm(suggestion.candidateMood, feelings)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sounds right")
                    }
                    OutlinedButton(
                        onClick = { showActions = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Not quite")
                    }
                }
            } else {
                Text(
                    text = "Log your mood instead",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                MoodQuickPicker(
                    onMoodSelected = { mood ->
                        onConfirm(mood, emptyList())
                    }
                )
            }
        }
    }
}

@Composable
private fun MoodQuickPicker(onMoodSelected: (Int) -> Unit) {
    val moods = listOf(
        1 to "Very Low",
        2 to "Low",
        3 to "Okay",
        4 to "Good",
        5 to "Great"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        moods.forEach { (value, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = { onMoodSelected(value) }) {
                    Text(
                        text = moodEmoji(value),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun moodEmoji(mood: Int) = when (mood) {
    1 -> "😞"
    2 -> "😕"
    3 -> "😐"
    4 -> "🙂"
    5 -> "😄"
    else -> "😐"
}
