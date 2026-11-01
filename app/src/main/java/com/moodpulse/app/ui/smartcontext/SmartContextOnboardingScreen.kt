package com.moodpulse.app.ui.smartcontext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.moodpulse.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartContextOnboardingScreen(
    hasPermission: Boolean,
    onOpenPermissionSettings: () -> Unit,
    onRefreshPermission: () -> Unit,
    onEnable: () -> Unit,
    onBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Smart Context",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        },
        containerColor = ObsidianBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Understand your digital patterns",
                fontFamily = HeadlineFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )

            Text(
                text = "Smart Context observes high-level app category usage locally on your phone — detecting trends like marathon social media sessions or late-night browsing to offer gentle, mindful check-in suggestions.",
                fontSize = 13.sp,
                color = TextLight.copy(alpha = 0.65f),
                lineHeight = 19.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x08FFFFFF))
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrivacyPoint(
                        emoji = "🔒",
                        title = "100% on-device private",
                        description = "No usage data ever leaves your device. Everything stays in app-private storage."
                    )
                    HorizontalDivider(color = Color(0x0FFFFFFF))
                    PrivacyPoint(
                        emoji = "📊",
                        title = "Category-level only",
                        description = "We only tally app categories (e.g. Social, Shopping). We never inspect contents, messages, or payments."
                    )
                    HorizontalDivider(color = Color(0x0FFFFFFF))
                    PrivacyPoint(
                        emoji = "⏸",
                        title = "Mindful pause alerts",
                        description = "Helps you catch distraction spirals or impulse browsing with a gentle pause before making purchases."
                    )
                    HorizontalDivider(color = Color(0x0FFFFFFF))
                    PrivacyPoint(
                        emoji = "⚙️",
                        title = "Full control anytime",
                        description = "Disable anytime from Settings, customize quiet hours, or delete all logged records instantly."
                    )
                }
            }

            Text(
                text = "REQUIRED PERMISSION",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = TextLight.copy(alpha = 0.35f)
            )

            Text(
                text = "Android requires \"Usage Access\" permission in System Settings to tally foreground app categories. Toggle MoodPulse on in the settings screen.",
                fontSize = 12.sp,
                color = TextLight.copy(alpha = 0.6f),
                lineHeight = 17.sp
            )

            if (!hasPermission) {
                OutlinedButton(
                    onClick = onOpenPermissionSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MintAccent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0x33A8D5A2))
                    )
                ) {
                    Text("Open Usage Access in Settings", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1A00C853))
                        .border(1.dp, Color(0x3300C853), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text("✓", fontSize = 14.sp, color = EmeraldGlow, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Usage Access permission granted",
                        fontSize = 13.sp,
                        color = EmeraldGlow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onEnable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGlow,
                    contentColor = Color(0xFF003D14)
                )
            ) {
                Text(
                    text = if (hasPermission) "Enable Smart Context" else "Grant Permission in Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PrivacyPoint(emoji: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 16.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextLight.copy(alpha = 0.5f),
                lineHeight = 15.sp
            )
        }
    }
}
