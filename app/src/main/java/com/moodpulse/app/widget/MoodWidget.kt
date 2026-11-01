package com.moodpulse.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.moodpulse.app.MoodPulseApp
import com.moodpulse.app.R
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.Mood
import kotlinx.coroutines.flow.first
import java.time.ZoneId

class MoodWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MoodPulseApp
        val recentEntry = app.container.moodRepository.getRecentEntries(1).first().firstOrNull()
        val lastMoodValue = recentEntry?.mood

        provideContent {
            MoodWidgetContent(
                lastMoodValue = lastMoodValue,
                context = context
            )
        }
    }
}

@Composable
private fun MoodWidgetContent(lastMoodValue: Int?, context: Context) {
    val bgColor = ColorProvider(Color(0xFFF2EDEB))
    val textColor = ColorProvider(Color(0xFF1D1B1A))
    val mutedColor = ColorProvider(Color(0xFF767870))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App name
            Text(
                text = "MoodPulse",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF4A5340)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(GlanceModifier.height(6.dp))

            // Last mood indicator
            if (lastMoodValue != null) {
                val mood = Mood.fromValueOrNull(lastMoodValue)
                Text(
                    text = mood?.emoji ?: "😐",
                    style = TextStyle(fontSize = 20.sp)
                )
                Text(
                    text = mood?.label ?: "Okay",
                    style = TextStyle(color = mutedColor, fontSize = 10.sp)
                )
                Spacer(GlanceModifier.height(6.dp))
            }

            // Mood tap row
            Text(
                text = "How are you?",
                style = TextStyle(color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(GlanceModifier.height(8.dp))

            Row(horizontalAlignment = Alignment.CenterHorizontally) {
                Mood.entries.forEach { mood ->
                    val key = ActionParameters.Key<Int>("mood_value")
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .clickable(
                                actionRunCallback<LogMoodFromWidgetCallback>(
                                    actionParametersOf(key to mood.value)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mood.emoji,
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                }
            }
        }
    }
}

class LogMoodFromWidgetCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val moodValue = parameters[ActionParameters.Key<Int>("mood_value")] ?: return
        val app = context.applicationContext as MoodPulseApp
        val entry = MoodEntry(
            mood = moodValue,
            timestampMs = System.currentTimeMillis(),
            timezoneId = ZoneId.systemDefault().id,
            source = "widget"
        )
        app.container.moodRepository.saveEntry(entry)
        // Refresh widget
        MoodWidget().update(context, glanceId)
    }
}

class MoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoodWidget()
}
