package com.moodpulse.app.ui.smartcontext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodpulse.app.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartContextOnboardingScreen(
    hasPermission: Boolean,
    onOpenPermissionSettings: () -> Unit,
    onEnable: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Context") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Understand your digital patterns",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Smart Context observes how you use your phone — not what you do in apps, but general patterns like spending more time on social media or less time on productivity — to gently suggest how you might be feeling.",
                style = MaterialTheme.typography.bodyLarge
            )

            PrivacyPoint(title = "Stays on your device", description = "No data ever leaves your phone. Usage patterns are stored only in the app's private storage.")
            PrivacyPoint(title = "High-level only", description = "We only see which category of app you used and for how long — never your messages, feeds, or purchases.")
            PrivacyPoint(title = "You're always in control", description = "Turn it off anytime. Exclude apps from tracking. Delete all data in Settings.")
            PrivacyPoint(title = "Non-judgemental suggestions", description = "Any mood hints are tentative questions, not diagnoses. Accept, correct, or dismiss them freely.")

            HorizontalDivider()

            Text(
                text = "Required permission",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Android requires you to grant \"Usage Access\" in Settings. This permission cannot be requested by the app directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (!hasPermission) {
                OutlinedButton(
                    onClick = onOpenPermissionSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Usage Access Settings")
                }
                Text(
                    text = "After granting, come back here and tap Enable below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    text = "Permission granted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onEnable,
                enabled = hasPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Smart Context")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacyPoint(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
