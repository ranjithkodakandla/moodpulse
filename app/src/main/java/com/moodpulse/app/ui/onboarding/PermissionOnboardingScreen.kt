package com.moodpulse.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionOnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pages = remember { onboardingPages() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F0D))
    ) {
        when (currentPage) {
            0 -> WelcomePage(onNext = { currentPage = 1 })
            1 -> PermissionPage(
                page = pages[0],
                pageIndex = 0,
                totalPages = pages.size,
                onNext = { currentPage = 2 }
            )
            2 -> PermissionPage(
                page = pages[1],
                pageIndex = 1,
                totalPages = pages.size,
                onNext = { onFinished() }
            )
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF00C853).copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🌿", fontSize = 52.sp)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Welcome to MoodPulse",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Your private mood journal.\nEverything stays on your phone.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = "To work best, MoodPulse needs\na couple of quick permissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853)
            )
        ) {
            Text(
                "Let's Go",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0C0F0D)
            )
        }
    }
}

data class PermissionPageData(
    val emoji: String,
    val title: String,
    val description: String,
    val why: String,
    val buttonLabel: String,
    val skipLabel: String? = null,
    val isRuntime: Boolean = false,
    val runtimePermission: String? = null
)

private fun onboardingPages() = listOf(
    PermissionPageData(
        emoji = "🔔",
        title = "Daily Check-in Reminders",
        description = "Get a gentle nudge to log your mood.",
        why = "Reminders help you build a consistent journaling habit. You can always change or turn them off in Settings.",
        buttonLabel = "Allow Notifications",
        skipLabel = "Skip for now",
        isRuntime = true,
        runtimePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.POST_NOTIFICATIONS else null
    ),
    PermissionPageData(
        emoji = "📊",
        title = "Smart Context (Optional)",
        description = "Let MoodPulse learn from your app usage patterns to suggest check-ins at the right moment.",
        why = "Only category-level usage is collected (e.g. 'Social' or 'Shopping') — never what you typed or viewed. All analysis is local.",
        buttonLabel = "Enable in Settings",
        skipLabel = "Not now",
        isRuntime = false
    )
)

@Composable
private fun PermissionPage(
    page: PermissionPageData,
    pageIndex: Int,
    totalPages: Int,
    onNext: () -> Unit
) {
    var granted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        onNext()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            repeat(totalPages) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == pageIndex) 20.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (i == pageIndex) Color(0xFF00C853)
                            else Color.White.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(page.emoji, fontSize = 64.sp)

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Why card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.07f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = page.why,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (page.isRuntime && page.runtimePermission != null) {
                        launcher.launch(page.runtimePermission)
                    } else {
                        onNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853)
                )
            ) {
                Text(
                    page.buttonLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0C0F0D)
                )
            }

            if (page.skipLabel != null) {
                TextButton(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        page.skipLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
