package com.moodpulse.app.ui.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.smartcontext.SmartContextUiState
import com.moodpulse.app.ui.theme.*
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onRemindersToggled: (Boolean) -> Unit,
    onReminderTimeSet: (Int, Int) -> Unit,
    onThemeChanged: (String) -> Unit,
    onExportJson: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
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
                title = {
                    Text(
                        "Settings",
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Gemma On-device AI Card (Prototype Inspired) ──
            item {
                GemmaAiSettingsCard()
            }

            // ── Smart Context & Digital Habits ──
            item { SettingsSectionHeader("Smart Context & Digital Habits") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = AppIcons.Psychology,
                        title = "Smart Context",
                        subtitle = if (smartContextState.prefs.monitoringEnabled)
                            "Monitoring usage patterns on-device"
                        else
                            "Off — tap to enable",
                        action = {
                            Switch(
                                checked = smartContextState.prefs.monitoringEnabled,
                                onCheckedChange = onSmartContextToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF003D14),
                                    checkedTrackColor = EmeraldGlow
                                )
                            )
                        }
                    )
                }
            }

            // ── Reminders ──
            item { SettingsSectionHeader("Reminders") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = AppIcons.Notifications,
                        title = "Daily reminder",
                        subtitle = "Get nudged to check in",
                        action = {
                            Switch(
                                checked = uiState.preferences.remindersEnabled,
                                onCheckedChange = onRemindersToggled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF003D14),
                                    checkedTrackColor = EmeraldGlow
                                )
                            )
                        }
                    )
                    if (uiState.preferences.remindersEnabled) {
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsRow(
                            icon = AppIcons.Schedule,
                            title = "Reminder time",
                            subtitle = formatTime(
                                uiState.preferences.reminderHour,
                                uiState.preferences.reminderMinute
                            ),
                            action = {
                                TextButton(onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute -> onReminderTimeSet(hour, minute) },
                                        uiState.preferences.reminderHour,
                                        uiState.preferences.reminderMinute,
                                        false
                                    ).show()
                                }) {
                                    Text("Change", color = EmeraldGlow, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }

            // ── Appearance ──
            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsCard {
                    val themes = listOf(
                        Triple("system", "System default", AppIcons.BrightnessAuto),
                        Triple("light", "Light", AppIcons.LightMode),
                        Triple("dark", "Dark", AppIcons.DarkMode)
                    )
                    themes.forEachIndexed { idx, (mode, label, icon) ->
                        SettingsRow(
                            icon = icon,
                            title = label,
                            action = {
                                RadioButton(
                                    selected = uiState.preferences.themeMode == mode,
                                    onClick = { onThemeChanged(mode) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = EmeraldGlow
                                    )
                                )
                            }
                        )
                        if (idx < themes.size - 1) {
                            HorizontalDivider(color = Color(0x14FFFFFF))
                        }
                    }
                }
            }

            // ── Data & Privacy ──
            item { SettingsSectionHeader("Data & Privacy") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = AppIcons.Download,
                        title = "Export as JSON",
                        subtitle = "Full backup (${uiState.totalEntries} entries)",
                        onClick = {
                            val filename = "moodpulse_backup_${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.json"
                            jsonExportLauncher.launch(filename)
                        }
                    )
                    HorizontalDivider(color = Color(0x14FFFFFF))
                    SettingsRow(
                        icon = AppIcons.TableChart,
                        title = "Export as CSV",
                        subtitle = "Open in spreadsheet apps",
                        onClick = {
                            val filename = "moodpulse_entries_${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.csv"
                            csvExportLauncher.launch(filename)
                        }
                    )
                    HorizontalDivider(color = Color(0x14FFFFFF))
                    SettingsRow(
                        icon = AppIcons.Upload,
                        title = "Restore from backup",
                        subtitle = "Import a JSON backup",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                    )
                    HorizontalDivider(color = Color(0x14FFFFFF))
                    SettingsRow(
                        icon = AppIcons.DeleteForever,
                        title = "Delete all data",
                        titleColor = MoodVeryLow,
                        subtitle = "This cannot be undone",
                        onClick = onDeleteAllRequest
                    )
                }
            }

            // ── App Info & Privacy Statement ──
            item { SettingsSectionHeader("About & Compliance") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = appVersion
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x06FFFFFF))
                        .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🔒 All mood data in app-private SQLite. Never transmitted.",
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "✦ Gemma AI runs 100% on-device. Zero data leaves your phone.",
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "📵 No analytics, no ads, no cloud backend, no tracking SDKs.",
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("Delete all data?", color = TextLight, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all ${uiState.totalEntries} mood check-ins. This action cannot be reversed.", color = TextLight.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(
                    onClick = onDeleteAllConfirm,
                    colors = ButtonDefaults.textButtonColors(contentColor = MoodVeryLow)
                ) { Text("Delete all", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) { Text("Cancel", color = TextLight.copy(alpha = 0.6f)) }
            },
            containerColor = ObsidianCard
        )
    }

    // Feedback message snackbar
    val message = uiState.importSuccess ?: uiState.importError ?: uiState.exportError
    if (message != null) {
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            onClearMessages()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = if (uiState.importError != null || uiState.exportError != null)
                Color(0xFF93000A)
            else EmeraldGlow
        ) {
            Text(message, color = if (uiState.importError != null || uiState.exportError != null) Color.White else Color(0xFF003D14))
        }
    }
}

@Composable
private fun GemmaAiSettingsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0x1F00C853), Color(0x0A004F1A))
                )
            )
            .border(1.dp, Color(0x3800C853), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0x3300C853)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", fontSize = 16.sp, color = EmeraldGlow)
                }
                Column {
                    Text(
                        text = "Gemma On-device AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow
                    )
                    Text(
                        text = "Free · INT4 Quant · Runs fully offline",
                        fontSize = 10.sp,
                        color = TextLight.copy(alpha = 0.45f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "Smart feeling tag suggestions from your notes",
                    "Personalised weekly mood insights",
                    "Conversational check-in companion",
                    "Pattern detection across 30+ days",
                    "Mindful spending & distraction pause warnings"
                ).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✦", fontSize = 8.sp, color = EmeraldGlow)
                        Text(
                            text = feature,
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(EmeraldGlow)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⬇ Download Gemma 2B",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF003D14)
                )
            }

            Text(
                text = "One-time download. All inference runs on your device. Zero data leaves your phone.",
                fontSize = 9.sp,
                color = TextLight.copy(alpha = 0.35f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp,
        color = TextLight.copy(alpha = 0.35f),
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = TextLight,
    action: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0x14FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextLight.copy(alpha = 0.8f),
                    modifier = Modifier.size(17.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = TextLight.copy(alpha = 0.4f)
                    )
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
