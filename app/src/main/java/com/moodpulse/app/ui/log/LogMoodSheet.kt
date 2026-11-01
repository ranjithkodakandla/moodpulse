package com.moodpulse.app.ui.log

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.FeelingTagChip
import com.moodpulse.app.ui.components.GlossyMoodOrb
import com.moodpulse.app.ui.components.MoodOrbSelectButton
import com.moodpulse.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogMoodSheet(
    state: LogMoodState,
    onMoodSelected: (Mood) -> Unit,
    onTagToggled: (FeelingTag) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val keyboard = LocalSoftwareKeyboardController.current

    val currentMood = state.selectedMood ?: Mood.GOOD
    val moodValue = currentMood.value
    val accentColor = moodColor(moodValue)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianBg)
    ) {
        // ── How We Feel ambient gradient bleeds mood color from top ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.22f),
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x33FFFFFF))
                    .align(Alignment.CenterHorizontally)
            )

            // Header row with close & delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x14FFFFFF))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontSize = 12.sp, color = TextLight.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                }

                Text(
                    text = if (state.editingEntryId != null) "Edit check-in" else "How are you feeling?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )

                if (state.editingEntryId != null && onDelete != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22EF5350))
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑", fontSize = 14.sp)
                    }
                } else {
                    Spacer(Modifier.size(32.dp))
                }
            }

            // ── Mood Hero with 68dp 3D Glossy Sphere & Halo ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.28f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GlossyMoodOrb(moodValue = moodValue, size = 68.dp)
                }

                Text(
                    text = currentMood.label,
                    fontFamily = HeadlineFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Text(
                    text = currentMood.description,
                    fontSize = 11.sp,
                    color = TextLight.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }

            // ── 5 Horizontal Mood Selector Orbs ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Mood.entries.forEach { mood ->
                    MoodOrbSelectButton(
                        mood = mood,
                        selected = state.selectedMood == mood,
                        onClick = { onMoodSelected(mood) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Add feeling tags ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ADD A FEELING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = TextLight.copy(alpha = 0.35f)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeelingTag.entries.forEach { tag ->
                        FeelingTagChip(
                            tag = tag,
                            selected = tag in state.selectedTags,
                            onClick = { onTagToggled(tag) }
                        )
                    }
                }
            }

            // ── Gemma AI Suggestions Row ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("✦", fontSize = 11.sp, color = EmeraldGlow)
                    Text(
                        text = "GEMMA SUGGESTS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = EmeraldGlow
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val aiTagSuggestions = remember(currentMood) {
                        when (currentMood) {
                            Mood.GREAT -> listOf(FeelingTag.ENERGISED, FeelingTag.FOCUSED, FeelingTag.HAPPY)
                            Mood.GOOD -> listOf(FeelingTag.CALM, FeelingTag.HAPPY, FeelingTag.FOCUSED)
                            Mood.OKAY -> listOf(FeelingTag.CALM, FeelingTag.TIRED)
                            Mood.LOW -> listOf(FeelingTag.TIRED, FeelingTag.STRESSED)
                            Mood.VERY_LOW -> listOf(FeelingTag.ANXIOUS, FeelingTag.FRUSTRATED, FeelingTag.STRESSED)
                        }
                    }

                    aiTagSuggestions.forEach { tag ->
                        val isSelected = tag in state.selectedTags
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) MoodGood.copy(alpha = 0.2f) else Color(0x1A00C853))
                                .border(1.dp, if (isSelected) MoodGood else Color(0x4D00C853), RoundedCornerShape(50))
                                .clickable { onTagToggled(tag) }
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(if (isSelected) "✓" else "+", fontSize = 10.sp, color = EmeraldGlow, fontWeight = FontWeight.Bold)
                                Text(tag.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGlow)
                            }
                        }
                    }
                }
            }

            // ── Note Field (Glass frosted) ──
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                placeholder = {
                    Text(
                        "Add a note… what's on your mind?",
                        fontSize = 13.sp,
                        color = TextLight.copy(alpha = 0.35f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0x1FFFFFFF),
                    focusedContainerColor = Color(0x0AFFFFFF),
                    unfocusedContainerColor = Color(0x06FFFFFF)
                ),
                maxLines = 4,
                singleLine = false
            )

            // ── Real-Time On-Device AI Note Tag Detection ──
            val detectedNoteTags = remember(state.note) {
                if (state.note.isBlank()) emptyList()
                else {
                    val lower = state.note.lowercase()
                    val list = linkedSetOf<FeelingTag>()
                    if (lower.contains("anxious") || lower.contains("worry") || lower.contains("panic") || lower.contains("nervous") || lower.contains("fear")) list.add(FeelingTag.ANXIOUS)
                    if (lower.contains("stress") || lower.contains("busy") || lower.contains("hectic") || lower.contains("overwhelm") || lower.contains("deadline") || lower.contains("pressure")) list.add(FeelingTag.STRESSED)
                    if (lower.contains("tired") || lower.contains("exhaust") || lower.contains("sleep") || lower.contains("drained") || lower.contains("fatigue")) list.add(FeelingTag.TIRED)
                    if (lower.contains("calm") || lower.contains("relax") || lower.contains("peace") || lower.contains("chill") || lower.contains("quiet")) list.add(FeelingTag.CALM)
                    if (lower.contains("happy") || lower.contains("great") || lower.contains("joy") || lower.contains("glad") || lower.contains("content")) list.add(FeelingTag.HAPPY)
                    if (lower.contains("focus") || lower.contains("work") || lower.contains("study") || lower.contains("code") || lower.contains("progress")) list.add(FeelingTag.FOCUSED)
                    if (lower.contains("frustrat") || lower.contains("annoy") || lower.contains("angry") || lower.contains("mad") || lower.contains("irritat")) list.add(FeelingTag.FRUSTRATED)
                    if (lower.contains("energ") || lower.contains("workout") || lower.contains("gym") || lower.contains("pump") || lower.contains("active")) list.add(FeelingTag.ENERGISED)
                    if (lower.contains("scroll") || lower.contains("insta") || lower.contains("shop") || lower.contains("buy")) {
                        list.add(FeelingTag.STRESSED)
                        list.add(FeelingTag.TIRED)
                    }
                    list.toList()
                }
            }

            if (detectedNoteTags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✦", fontSize = 10.sp, color = EmeraldGlow)
                        Text(
                            text = "GEMMA DETECTED FROM YOUR NOTE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = EmeraldGlow
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        detectedNoteTags.forEach { tag ->
                            val isSelected = tag in state.selectedTags
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) EmeraldGlow.copy(alpha = 0.25f) else Color(0x1A00C853))
                                    .border(1.dp, if (isSelected) EmeraldGlow else Color(0x4D00C853), RoundedCornerShape(50))
                                    .clickable { onTagToggled(tag) }
                                    .padding(horizontal = 11.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(if (isSelected) "✓" else "+", fontSize = 10.sp, color = EmeraldGlow, fontWeight = FontWeight.Bold)
                                    Text(tag.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGlow)
                                }
                            }
                        }
                    }
                }
            }

            // ── Full-Width Pill Gradient Save Button ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = accentColor,
                        spotColor = accentColor
                    )
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .clickable(
                        enabled = state.selectedMood != null && !state.isSaving
                    ) {
                        keyboard?.hide()
                        onSave()
                    }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (state.editingEntryId != null) "Save changes" else "Save check-in",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
