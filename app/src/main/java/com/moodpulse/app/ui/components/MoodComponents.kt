package com.moodpulse.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 3D Glossy Mood Orb rendered with hardware-accelerated Canvas.
 * Implements radial gradients, specular highlights, and custom mood glyphs.
 */
@Composable
fun GlossyMoodOrb(
    moodValue: Int,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val (c1, c2) = when (moodValue) {
        5 -> Pair(OrbGreat1, OrbGreat2)
        4 -> Pair(OrbGood1, OrbGood2)
        3 -> Pair(OrbOkay1, OrbOkay2)
        2 -> Pair(OrbLow1, OrbLow2)
        else -> Pair(OrbVeryLow1, OrbVeryLow2)
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = w * 0.47f
        val center = Offset(w / 2f, h / 2f)

        // 1. Base sphere with 3D radial gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c1, c2),
                center = Offset(w * 0.38f, h * 0.32f),
                radius = r * 1.36f
            ),
            radius = r,
            center = center
        )

        // 2. Subtle rim outline
        drawCircle(
            color = Color.Black.copy(alpha = 0.25f),
            radius = r,
            center = center,
            style = Stroke(width = w * 0.03f)
        )

        // 3. Specular gloss gradient overlay
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.42f),
                    0.6f to Color.White.copy(alpha = 0.08f),
                    1.0f to Color.Transparent
                ),
                center = Offset(w * 0.35f, h * 0.22f),
                radius = r * 1.1f
            ),
            radius = r,
            center = center
        )

        // 4. Specular highlight reflection ellipse (rotated top-left)
        rotate(-18f, pivot = Offset(w * 0.41f, h * 0.32f)) {
            drawOval(
                color = Color.White.copy(alpha = 0.26f),
                topLeft = Offset(w * 0.41f - w * 0.26f, h * 0.32f - h * 0.14f),
                size = Size(w * 0.52f, h * 0.28f)
            )
        }

        // 5. Centered crisp white mood symbol
        val symColor = Color.White.copy(alpha = 0.92f)
        val strokeWidth = (w * 0.095f).coerceAtLeast(1.5f)

        when (moodValue) {
            5 -> {
                // 4-pointed sparkle / star
                val starPath = Path().apply {
                    val cx = w * 0.5f
                    val cy = h * 0.5f
                    val outer = w * 0.28f
                    val inner = w * 0.07f
                    moveTo(cx, cy - outer)
                    lineTo(cx + inner, cy - inner)
                    lineTo(cx + outer, cy)
                    lineTo(cx + inner, cy + inner)
                    lineTo(cx, cy + outer)
                    lineTo(cx - inner, cy + inner)
                    lineTo(cx - outer, cy)
                    lineTo(cx - inner, cy - inner)
                    close()
                }
                drawPath(starPath, color = symColor)
            }
            4 -> {
                // Happy upward smile curve
                val smilePath = Path().apply {
                    moveTo(w * 0.275f, h * 0.675f)
                    quadraticTo(w * 0.5f, h * 0.325f, w * 0.725f, h * 0.675f)
                }
                drawPath(
                    smilePath,
                    color = symColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            3 -> {
                // Calm neutral double horizontal line
                drawLine(
                    color = symColor,
                    start = Offset(w * 0.275f, h * 0.45f),
                    end = Offset(w * 0.725f, h * 0.45f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = symColor,
                    start = Offset(w * 0.275f, h * 0.625f),
                    end = Offset(w * 0.725f, h * 0.625f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            2 -> {
                // Gentle frown curve
                val frownPath = Path().apply {
                    moveTo(w * 0.275f, h * 0.425f)
                    quadraticTo(w * 0.5f, h * 0.75f, w * 0.725f, h * 0.425f)
                }
                drawPath(
                    frownPath,
                    color = symColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            1 -> {
                // Deep double frown curve
                val frown1 = Path().apply {
                    moveTo(w * 0.25f, h * 0.40f)
                    quadraticTo(w * 0.5f, h * 0.725f, w * 0.75f, h * 0.40f)
                }
                val frown2 = Path().apply {
                    moveTo(w * 0.325f, h * 0.575f)
                    quadraticTo(w * 0.5f, h * 0.825f, w * 0.675f, h * 0.575f)
                }
                drawPath(
                    frown1,
                    color = symColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawPath(
                    frown2,
                    color = symColor.copy(alpha = 0.6f),
                    style = Stroke(width = strokeWidth * 0.75f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Compact horizontal mood orb selector button (used in 5-orb row in LogMoodSheet).
 */
@Composable
fun MoodOrbSelectButton(
    mood: Mood,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val accentColor = moodColor(mood.value)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) accentColor.copy(alpha = 0.12f)
                else Color(0x0AFFFFFF)
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accentColor else Color(0x14FFFFFF),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(role = Role.RadioButton) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accentColor else TextLight.copy(alpha = 0.5f)
        )
    }
}

/**
 * Feeling Tag Chip styled as modern obsidian glass pill.
 */
@Composable
fun FeelingTagChip(
    tag: FeelingTag,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val selectedBorder = MoodGood
    val selectedBg = MoodGood.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) selectedBg else Color(0x0DFFFFFF))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) selectedBorder else Color(0x1AFFFFFF),
                shape = RoundedCornerShape(50)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(tag.emoji, fontSize = 13.sp)
            Text(
                text = tag.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MoodGood else TextLight.copy(alpha = 0.7f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Beautiful Daylio-style Mood Entry Card with glowing left bar and frosted glass background.
 */
@Composable
fun MoodEntryCard(
    entry: MoodEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mood = Mood.fromValue(entry.mood)
    val color = moodColor(entry.mood)

    val timeStr = remember(entry.timestampMs) {
        val instant = Instant.ofEpochMilli(entry.timestampMs)
        val zone = runCatching { ZoneId.of(entry.timezoneId) }.getOrDefault(ZoneId.systemDefault())
        val time = instant.atZone(zone).toLocalTime()
        time.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Glowing left bar
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .fillMaxHeight()
                .matchParentSize()
                .background(color)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlossyMoodOrb(moodValue = entry.mood, size = 32.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mood.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )

                if (entry.feelingTags.isNotEmpty()) {
                    Text(
                        text = entry.feelingTags.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MintAccent,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight.copy(alpha = 0.45f),
                        maxLines = 1,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = TextLight.copy(alpha = 0.4f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextLight.copy(alpha = 0.38f),
            letterSpacing = 1.2.sp
        )
        action?.invoke()
    }
}

@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFF8F00))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = "🔥 $days",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun EmptyState(
    emoji: String = "📊",
    message: String,
    subMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x08FFFFFF))
            .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 36.sp)
        Text(
            message,
            style = MaterialTheme.typography.titleMedium,
            color = TextLight.copy(alpha = 0.85f),
            fontWeight = FontWeight.SemiBold
        )
        if (subMessage != null) {
            Text(
                subMessage,
                style = MaterialTheme.typography.bodySmall,
                color = TextLight.copy(alpha = 0.4f)
            )
        }
    }
}

// Compatibility alias
@Composable
fun MoodButton(
    mood: Mood,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = MoodOrbSelectButton(mood = mood, selected = selected, onClick = onClick, modifier = modifier)
