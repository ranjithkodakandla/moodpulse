# MoodPulse

A simple, local-first Android mood tracker. Log how you feel in seconds. No account, no cloud, no ads.

## Product

**Three connected experiences:**

1. **Capture** — Tap your mood. Takes 2 seconds. Optional feeling tags and a note.
2. **Understand** — Calendar history, daily timeline, see your mood at a glance.
3. **Reflect** — Weekly Pulse shows mood distribution, time-of-day patterns, and trend vs the prior week.

**Mood scale:** 😩 Very Low → 😔 Low → 😐 Okay → 🙂 Good → 😊 Great

**Feeling tags (optional):** Calm, Happy, Anxious, Stressed, Tired, Energised, Focused, Frustrated

**Home-screen widget:** Tap any mood emoji directly on your home screen. Saves without opening the app.

## Privacy

- All data lives on your device in a local SQLite database
- No internet permission — zero data leaves the device
- Export/import as JSON or CSV at any time
- Delete all data with one tap in Settings

## Build

**Requirements:**
- Android Studio (Meerkat or later recommended)
- JDK 17+
- Android SDK 35

**Steps:**
```bash
git clone https://github.com/ranjithkodakandla/moodpulse.git
cd moodpulse
# Open in Android Studio → Build → Generate Signed Bundle/APK
# Or from command line (after SDK setup):
./gradlew assembleDebug
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`

**Install on device:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Widget | Jetpack Glance |
| Database | Room / SQLite |
| Preferences | DataStore |
| Reminders | WorkManager |
| Architecture | MVVM + Repository |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |

**Package structure:**
```
com.moodpulse.app/
  data/
    db/           Room entities, DAO, Database
    repository/   MoodRepository
    datastore/    User preferences (theme, reminders)
  domain/
    Mood.kt       Mood enum, FeelingTag enum, TimeOfDay enum
    InsightsEngine.kt  Deterministic weekly insights calculation
    ExportImport.kt    JSON + CSV export/import
  ui/
    home/         Today screen + ViewModel
    log/          Mood logging bottom sheet + ViewModel
    history/      Calendar history screen + ViewModel
    insights/     Weekly Pulse screen + ViewModel
    settings/     Settings screen + ViewModel
    navigation/   NavGraph + bottom nav
    theme/        Color, Typography, Theme
    components/   Shared Composables
  widget/         Glance widget + LogMoodFromWidgetCallback
  worker/         ReminderWorker + ReminderScheduler + BootReceiver
```

## Features

### Mood Logging
- Bottom sheet with 5 mood cards (immediate haptic feedback)
- Optional feeling tags (8 available)
- Optional free-text note (up to 4 lines)
- Multiple entries per day supported
- Edit and delete any entry

### Home Screen Widget
- Direct mood selection from home screen
- Saves to same Room database as app
- Shows last recorded mood
- Works when app is not running
- Refreshes after each log

### History
- Monthly calendar with mood dots
- Navigate any past day to see its entries
- Colour-coded mood bars on entry cards

### Weekly Pulse
- Stat strip: this week / all time / streak
- Mood distribution bar chart
- Time of day breakdown (morning/afternoon/evening/night)
- Weekly grid (entries per day of week)
- Trend vs previous week (improving / declining / stable)
- Honest handling of insufficient data (<3 entries)

### Settings
- Daily reminder toggle + time picker
- Theme: system / light / dark
- Export as JSON (full backup)
- Export as CSV (spreadsheet-compatible)
- Restore from JSON backup
- Delete all data (with confirmation)
- Privacy summary
- App version

## Automated Tests

28 JVM unit tests covering:
- All mood/tag/time-of-day enum behaviour
- Weekly insights computation (avg, distribution, trend, streak)
- JSON/CSV export round trip
- Import validation and error cases

Run tests in Android Studio or:
```bash
./gradlew test
```

## Fonts

The design calls for **Epilogue** (headlines) and **Manrope** (body text) from Google Fonts.
The shipped version uses system fonts. To add the custom fonts:

1. Download TTF files from [fonts.google.com](https://fonts.google.com)
2. Place in `app/src/main/res/font/`:
   - `epilogue_regular.ttf`, `epilogue_medium.ttf`, `epilogue_semibold.ttf`, `epilogue_bold.ttf`
   - `manrope_regular.ttf`, `manrope_medium.ttf`, `manrope_semibold.ttf`, `manrope_bold.ttf`
3. Update `Type.kt` to use `FontFamily(Font(R.font.epilogue_regular, FontWeight.Normal), ...)`

## Known Limitations

1. **No APK pre-built** — Android SDK (dl.google.com) was blocked during development. Build locally with Android Studio.
2. **System fonts** — Custom Epilogue/Manrope fonts need to be added manually (see above).
3. **Widget on Android 12+** — Requires targeting API 31+ for full widget configurability. Currently uses basic Glance widget.
4. **Lock-screen widget** — Not implemented. Home-screen widget works as designed.
5. **No Room database tests** — Android instrumented tests need a physical device or emulator.
6. **Reminders on Android 13+** — POST_NOTIFICATIONS permission must be granted by the user at runtime.

## Release Signing

For a release build, create `keystore.properties` in the project root:
```
storeFile=/path/to/your.keystore
storePassword=yourpassword
keyAlias=youralias
keyPassword=yourpassword
```

**Do not commit this file.** Add it to `.gitignore`.

Then add signing config to `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        // read from keystore.properties
    }
}
```

## Licence

MIT — see LICENCE file.
