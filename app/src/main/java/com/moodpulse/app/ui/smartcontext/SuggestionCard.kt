package com.moodpulse.app.ui.smartcontext

import androidx.compose.animation.AnimatedVisibility
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
    onConfirm: (Long, Int, List<String>) -> Unit,
    onCorrect: (Long, Int, List<String>, String?) -> Unit,
    onAcknowledge: (Long, Boolean, String?) -> Unit,
    onDismiss: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCorrection by remember { mutableStateOf(false) }
    var showCommentBox by remember { mutableStateOf(false) }
    var userComment by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<Int?>(null) }

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
            // Header with badge and Dismiss & Snooze buttons
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Snooze 1h button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x14FFFFFF))
                            .clickable { onSnooze(suggestion.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Snooze 1h",
                            fontSize = 10.sp,
                            color = TextLight.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Dismiss (X)
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
            }

            // Prompt text
            Text(
                text = suggestion.promptText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                lineHeight = 20.sp
            )

            // Primary response actions
            if (!showCorrection) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sounds right (Confirm)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(EmeraldGlow)
                            .clickable {
                                val feelings = suggestion.candidateFeelings
                                    .split(",").filter { it.isNotBlank() }
                                onConfirm(suggestion.id, suggestion.candidateMood, feelings)
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

                    // Not quite (Correct)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(50))
                            .clickable { showCorrection = true }
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

                // Secondary row: Like / Acknowledge & Add Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onAcknowledge(suggestion.id, true, null) },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("👍 Helpful insight", fontSize = 11.sp, color = MintAccent)
                    }

                    TextButton(
                        onClick = { showCommentBox = !showCommentBox },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showCommentBox) "Hide note" else "💬 Add comment",
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                // Expanded correction section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tap your actual mood:",
                        fontSize = 11.sp,
                        color = TextLight.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                    MoodQuickPicker(
                        selectedMood = selectedMood,
                        onMoodSelected = { mood ->
                            selectedMood = mood
                            onCorrect(suggestion.id, mood, emptyList(), userComment.ifBlank { null })
                        }
                    )
                }
            }

            // Optional comment box
            AnimatedVisibility(visible = showCommentBox) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = userComment,
                        onValueChange = { userComment = it },
                        placeholder = {
                            Text("Add your thoughts or reflections...", fontSize = 11.sp, color = TextLight.copy(alpha = 0.35f))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGlow,
                            unfocusedBorderColor = Color(0x24FFFFFF),
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            cursorColor = EmeraldGlow
                        ),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (userComment.isNotBlank()) {
                                    onAcknowledge(suggestion.id, true, userComment)
                                }
                            }
                        ) {
                            Text("Save Note", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGlow)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodQuickPicker(
    selectedMood: Int?,
    onMoodSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Mood.entries.forEach { mood ->
            val isSelected = selectedMood == mood.value
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0x3300C853) else Color.Transparent)
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
                    color = if (isSelected) EmeraldGlow else TextLight.copy(alpha = 0.7f)
                )
            }
        }
    }
}
