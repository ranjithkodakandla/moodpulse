package com.moodpulse.app.ui.settings

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moodpulse.app.smartcontext.SmartContextUiState
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onRemindersToggled: (Boolean) -> Unit,
    onReminderTimeSet: (Int, Int) -> Unit,
    onThemeChanged: (String) -> Unit,
    onExportJson: (android.net.Uri) -> Unit,
    onExportCsv: (android.net.Uri) -> Unit,
    onImport: (android.net.Uri) -> Unit,
    onDeleteAllRequest: () -> Unit,
    onDeleteAllConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onClearMessages: () -> Unit,
    appVersion: String,
    smartContextState: SmartContextUiState = SmartContextUiState(),
    onSmartContextToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    val jsonExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { onExportJson(it) } }

    val csvExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { onExportCsv(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImport(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reminders section
            item {
                SettingsSectionHeader("Reminders")
            }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Daily reminder",
                        subtitle = "Get nudged to check in",
                        action = {
                            Switch(
                                checked = uiState.preferences.remindersEnabled,
                                onCheckedChange = onRemindersToggled
                            )
                        }
                    )
                    if (uiState.preferences.remindersEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        SettingsRow(
                            icon = Icons.Outlined.Schedule,
                            title = "Reminder time",
                            subtitle = formatTime(uiState.preferences.reminderHour, uiState.preferences.reminderMinute),
                            action = {
                                TextButton(onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, h, m -> onReminderTimeSet(h, m) },
                                        uiState.preferences.reminderHour,
                                        uiState.preferences.reminderMinute,
                                        false
                                    ).show()
                                }) {
                                    Text("Change")
                                }
                            }
                        )
                    }
                }
            }

            // Appearance
            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsCard {
                    val themes = listOf("system" to "System default", "light" to "Light", "dark" to "Dark")
                    themes.forEachIndexed { idx, (mode, label) ->
                        SettingsRow(
                            icon = when (mode) {
                                "light" -> Icons.Outlined.LightMode
                                "dark" -> Icons.Outlined.DarkMode
                                else -> Icons.Outlined.BrightnessAuto
                            },
                            title = label,
                            action = {
                                RadioButton(
                                    selected = uiState.preferences.themeMode == mode,
                                    onClick = { onThemeChanged(mode) }
                                )
                            }
                        )
                        if (idx < themes.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Smart Context
            item { SettingsSectionHeader("Smart Context & Digital Habits") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Psychology,
                        title = "Smart Context",
                        subtitle = if (smartContextState.prefs.monitoringEnabled)
                            "Monitoring app usage patterns"
                        else
                            "Off — tap to enable",
                        action = {
                            Switch(
                                checked = smartContextState.prefs.monitoringEnabled,
                                onCheckedChange = onSmartContextToggle
                            )
                        }
                    )
                    if (smartContextState.prefs.monitoringEnabled && !smartContextState.hasPermission) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Usage Access permission is required for Smart Context to work.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Data
            item { SettingsSectionHeader("Data & Privacy") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Download,
                        title = "Export as JSON",
                        subtitle = "${uiState.totalEntries} entries",
                        onClick = {
                            val ts = LocalDateTime.now().toString().replace(":", "-").take(16)
                            jsonExportLauncher.launch("moodpulse-export-$ts.json")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Outlined.TableChart,
                        title = "Export as CSV",
                        subtitle = "Open in spreadsheet apps",
                        onClick = {
                            val ts = LocalDateTime.now().toString().replace(":", "-").take(16)
                            csvExportLauncher.launch("moodpulse-export-$ts.csv")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Outlined.Upload,
                        title = "Restore from backup",
                        subtitle = "Import a JSON backup",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Delete all data",
                        titleColor = MaterialTheme.colorScheme.error,
                        subtitle = "This cannot be undone",
                        onClick = onDeleteAllRequest
                    )
                }
            }

            // Privacy info
            item { SettingsSectionHeader("Privacy") }
            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🔒 All your mood data is stored locally on this device.", style = MaterialTheme.typography.bodySmall)
                        Text("📵 MoodPulse does not have internet access and never transmits your data.", style = MaterialTheme.typography.bodySmall)
                        Text("🗑 You can delete all data at any time from the option above.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // About
            item { SettingsSectionHeader("About") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        subtitle = appVersion
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("Delete all data?") },
            text = { Text("This will permanently delete all ${uiState.totalEntries} mood entries. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = onDeleteAllConfirm,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) { Text("Cancel") }
            }
        )
    }

    // Messages
    val message = uiState.importSuccess ?: uiState.importError ?: uiState.exportError
    if (message != null) {
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            onClearMessages()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = if (uiState.importError != null || uiState.exportError != null)
                MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(message)
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    action: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null)
        Modifier.fillMaxWidth()
    else Modifier.fillMaxWidth()

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = titleColor)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            action?.invoke()
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "$h:${minute.toString().padStart(2, '0')} $amPm"
}
