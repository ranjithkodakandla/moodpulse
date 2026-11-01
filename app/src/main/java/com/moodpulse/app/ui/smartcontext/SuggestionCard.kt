package com.moodpulse.app.ui.smartcontext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.moodpulse.app.data.db.MoodSuggestion
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.GlossyMoodOrb
import com.moodpulse.app.ui.theme.*

@Composable
fun SuggestionCard(
    suggestion: MoodSuggestion,
    onConfirm: (Int, List<String>) -> Unit,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showActions by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x1F00C853), Color(0x0A004F1A))
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
                    Text("✦", fontSize = 12.sp, color = EmeraldGlow)
                    Text(
                        text = "SMART CONTEXT CHECK-IN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = EmeraldGlow
                    )
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x14FFFFFF))
                        .clickable { onDismiss(suggestion.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(12.dp),
                        tint = TextLight.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                text = suggestion.promptText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                lineHeight = 20.sp
            )

            if (!showActions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(EmeraldGlow)
                            .clickable {
                                val feelings = suggestion.candidateFeelings
                                    .split(",").filter { it.isNotBlank() }
                                onConfirm(suggestion.candidateMood, feelings)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sounds right",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF003D14)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(50))
                            .clickable { showActions = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Not quite",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Tap your actual mood:",
                    fontSize = 11.sp,
                    color = TextLight.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Mood.entries.forEach { mood ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMoodSelected(mood.value) }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                GlossyMoodOrb(moodValue = mood.value, size = 28.dp)
                Text(
                    text = when (mood.value) {
                        1 -> "V. Low"
                        2 -> "Low"
                        3 -> "Okay"
                        4 -> "Good"
                        else -> "Great"
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextLight.copy(alpha = 0.7f)
                )
            }
        }
    }
}
