package com.moodpulse.app.smartcontext

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.moodpulse.app.data.db.BehaviouralObservation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class PhysicalActivityData(
    val todaySteps: Long = 0L,
    val exerciseMinutes: Int = 0,
    val sleepHours: Double? = null,
    val avgHeartRate: Double? = null,
    val source: String = "None", // "Health Connect", "Phone Step Sensor", "None"
    val isConnected: Boolean = false
)

class PhysicalActivityManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun hasHealthConnectPermission(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            permissions.any { it in granted }
        } catch (_: Throwable) {
            false
        }
    }

    fun hasSensorPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Reads today's activity stats from Health Connect (or fallback hardware pedometer).
     */
    suspend fun readTodayActivity(): PhysicalActivityData {
        val client = healthConnectClient
        if (client != null && hasHealthConnectPermission()) {
            try {
                val zoneId = ZoneId.systemDefault()
                val startOfDay = LocalDate.now().atStartOfDay(zoneId).toInstant()
                val now = Instant.now()

                // 1. Daily steps aggregate
                var steps = 0L
                try {
                    val stepsResponse = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(StepsRecord.COUNT_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                        )
                    )
                    steps = stepsResponse[StepsRecord.COUNT_TOTAL] ?: 0L
                } catch (_: Throwable) {}

                // 2. Exercise sessions
                var exerciseMinutes = 0
                try {
                    val exerciseRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = ExerciseSessionRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                        )
                    )
                    exerciseMinutes = exerciseRecords.records.sumOf {
                        java.time.Duration.between(it.startTime, it.endTime).toMinutes()
                    }.toInt()
                } catch (_: Throwable) {}

                // 3. Sleep sessions (from last night: previous day 18:00 to now)
                var sleepHours: Double? = null
                try {
                    val sleepRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = SleepSessionRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(
                                startOfDay.minus(12, ChronoUnit.HOURS),
                                now
                            )
                        )
                    )
                    val longestSleep = sleepRecords.records.maxByOrNull {
                        java.time.Duration.between(it.startTime, it.endTime).toMinutes()
                    }
                    if (longestSleep != null) {
                        val minutes = java.time.Duration.between(longestSleep.startTime, longestSleep.endTime).toMinutes()
                        sleepHours = (minutes / 60.0 * 10).toInt() / 10.0
                    }
                } catch (_: Throwable) {}

                // 4. Heart rate
                var avgHr: Double? = null
                try {
                    val hrRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                        )
                    )
                    val samples = hrRecords.records.flatMap { it.samples }
                    if (samples.isNotEmpty()) {
                        avgHr = (samples.map { it.beatsPerMinute }.average() * 10).toInt() / 10.0
                    }
                } catch (_: Throwable) {}

                return PhysicalActivityData(
                    todaySteps = steps,
                    exerciseMinutes = exerciseMinutes,
                    sleepHours = sleepHours,
                    avgHeartRate = avgHr,
                    source = "Health Connect",
                    isConnected = true
                )
            } catch (_: Throwable) {}
        }

        // Fallback: Check hardware step sensor
        val sensorSteps = readHardwareSensorSteps()
        return PhysicalActivityData(
            todaySteps = sensorSteps ?: 0L,
            source = if (sensorSteps != null) "Phone Step Sensor" else "None",
            isConnected = sensorSteps != null
        )
    }

    private fun readHardwareSensorSteps(): Long? {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return null
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null
        // On modern Android, reading instant value is event-driven; return null if no event received yet
        return null
    }

    /**
     * Converts physical activity data into cautious, non-medical behavioural observations.
     */
    fun evaluateActivityObservations(activity: PhysicalActivityData): List<BehaviouralObservation> {
        val list = mutableListOf<BehaviouralObservation>()
        val now = System.currentTimeMillis()

        // High physical activity
        if (activity.todaySteps >= 7500 || activity.exerciseMinutes >= 30) {
            list.add(
                BehaviouralObservation(
                    observationType = "PHYSICAL_ACTIVITY_HIGH",
                    dateTime = now,
                    baselineValue = 5000f,
                    currentValue = activity.todaySteps.toFloat(),
                    evidenceJson = """{"steps":${activity.todaySteps},"exerciseMinutes":${activity.exerciseMinutes},"source":"${activity.source}"}"""
                )
            )
        }

        // Short sleep recorded
        if (activity.sleepHours != null && activity.sleepHours in 1.0..5.8) {
            list.add(
                BehaviouralObservation(
                    observationType = "SHORT_SLEEP",
                    dateTime = now,
                    baselineValue = 7.5f,
                    currentValue = activity.sleepHours.toFloat(),
                    evidenceJson = """{"sleepHours":${activity.sleepHours},"source":"${activity.source}"}"""
                )
            )
        }

        // Restful sleep recorded
        if (activity.sleepHours != null && activity.sleepHours >= 7.5) {
            list.add(
                BehaviouralObservation(
                    observationType = "RESTFUL_SLEEP",
                    dateTime = now,
                    baselineValue = 7.0f,
                    currentValue = activity.sleepHours.toFloat(),
                    evidenceJson = """{"sleepHours":${activity.sleepHours},"source":"${activity.source}"}"""
                )
            )
        }

        return list
    }
}
