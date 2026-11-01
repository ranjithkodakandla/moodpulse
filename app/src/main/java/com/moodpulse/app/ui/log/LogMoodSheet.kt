package com.moodpulse.app.ui.log

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.components.FeelingTagChip
import com.moodpulse.app.ui.components.MoodButton

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.outlineVariant
            ) {}
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.editingEntryId != null) "Edit check-in" else "How are you?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                if (state.editingEntryId != null && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete entry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mood selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Mood.entries.reversed().forEach { mood ->
                MoodButton(
                    mood = mood,
                    selected = state.selectedMood == mood,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }

        // Feeling tags
        AnimatedVisibility(visible = state.selectedMood != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Add feeling (optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        }

        // Note field
        AnimatedVisibility(visible = state.selectedMood != null) {
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Add a note… (optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                maxLines = 4,
                singleLine = false
            )
        }

        // Save button
        Button(
            onClick = {
                keyboard?.hide()
                if (state.editingEntryId != null) onSave() else onSave()
            },
            enabled = state.selectedMood != null && !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = if (state.editingEntryId != null) "Save changes" else "Save",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
