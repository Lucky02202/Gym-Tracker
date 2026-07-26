# Gym Progress Tracker

A fully offline Android gym-tracking app built with Kotlin, Jetpack Compose, Room, and
Navigation Compose, following MVVM architecture. No internet, login, or cloud services
are used — all data lives in a local SQLite (Room) database on the device.

## Architecture

```
data/
  entity/       Room entities (Exercise, Workout, WorkoutExercise, WorkoutSet,
                 WorkoutSplit, WorkoutDay, BodyWeightEntry, Settings) + relation DTOs
  dao/          Room DAOs — one per entity family, with Flow-based reactive queries
  repository/   GymRepository — single source of truth used by every ViewModel
  util/         SplitGenerator, OneRepMaxCalculator, DateUtils, BackupManager
ui/
  theme/        Material 3 color scheme, typography, light/dark theme
  navigation/   Screen routes + GymNavGraph (NavHost + bottom navigation)
  viewmodel/    One ViewModel per screen/feature, built via a manual ViewModelFactory
  components/   Reusable composables (SetRow, StatCard, LineChart)
  screens/      Onboarding, Home, Today's Workout (+ active logging), History,
                 Progress, Statistics, Calendar, Search, Body Weight, Settings,
                 Exercise Library, Split Editor
```

### Key design notes

- **Split scheduling**: `SplitGenerator.spreadAcrossWeek()` evenly distributes your
  chosen number of training days across a 7-day week (Mon–Sun), filling the rest with
  rest days. "Today's Workout" simply looks up today's weekday slot in the active split.
- **Fast set entry**: `ActiveWorkoutViewModel.addSet()` copies the previous set's
  weight/reps into the new set, per spec, so you only edit what changed.
- **Charts**: progress/bodyweight charts are a small dependency-free `Canvas`-based
  line chart (`ui/components/LineChart.kt`) — no external charting library required.
- **Backup/Restore**: `BackupManager` copies the raw Room SQLite file to/from a
  location you choose via the system file picker (Storage Access Framework). Fully
  local — no cloud services involved. The app should be restarted after a restore so
  the new database is picked up cleanly.
- **No Hilt/Dagger**: given the app's size, dependency injection is handled manually
  via `GymTrackerApplication` (holds the DB + repository) and `ViewModelFactory`.

## Building the project in Android Studio

1. **Install Android Studio** (Koala/2024.1 or newer recommended) with the Android
   SDK for API 34 installed via the SDK Manager.
2. **Open the project**: `File > Open`, select the `GymProgressTracker` folder.
3. Android Studio will detect there's no Gradle wrapper JAR committed (it's a binary
   file and isn't included here). Do one of the following:
   - Let Android Studio regenerate it automatically when it prompts you to "Sync
     Project with Gradle Files" — it can create the wrapper for you, **or**
   - From a terminal with Gradle installed, run `gradle wrapper --gradle-version 8.7`
     inside the project root before opening it in Android Studio.
4. Click **Sync Now** when prompted. Gradle will download the Android Gradle Plugin,
   Kotlin, Compose, Room, and Navigation dependencies (requires an internet
   connection for this one-time dependency download — the *app itself* needs no
   internet at runtime).
5. Select the `app` run configuration and an emulator or physical device
   (**Android 8.0 / API 26 or newer**), then click **Run ▶**.

## Building a release APK

1. `Build > Generate Signed Bundle / APK...`
2. Choose **APK**, then create or select a signing key (`Build > Generate Signed
   Bundle / APK... > Create new...` if you don't have one yet).
3. Select the **release** build variant. This build enables R8 minification using
   the included `proguard-rules.pro` (which keeps Room's generated classes and all
   entity classes intact).
4. Click **Finish** — Android Studio will build `app-release.apk` under
   `app/release/`, ready to install (`adb install app-release.apk`) or distribute.

Alternatively, from the command line once the Gradle wrapper is generated:

```bash
./gradlew assembleRelease
# APK output: app/build/outputs/apk/release/app-release.apk
```

## Features implemented

- Onboarding split generator (2–6 days/week) with the exact recommended splits from
  the spec, plus a full split editor (rename days, mark as rest, switch frequency)
- Today's Workout screen that auto-detects the day (or shows "Today is your rest day")
- Fully custom exercises (name, muscle group, optional notes), favorites, search
- Unlimited exercises/sets per workout, with weight/reps/RIR/notes per set and
  automatic prefill from the previous set
- Permanent workout history with duration, volume, and set counts; tap into any past
  workout; duplicate a previous workout (with or without its sets)
- Per-exercise progress charts (estimated 1RM, weight, volume) and personal records
  (best weight, best 1RM, best session volume)
- Statistics dashboard (total workouts, current/longest streak, total sets/reps/
  weight lifted, average workout duration)
- Body weight & body fat % tracking with a trend chart
- Calendar view of workout days, tap-to-view
- Unified search across exercises and workout history
- Local backup/restore of the entire database via the system file picker
- Settings for units (kg/lbs), theme (light/dark/system), and default rest timer
- Material 3 UI with full light/dark theme support
