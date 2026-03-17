# Guided Fitness

A weekly guided fitness Android app with video integration and progress tracking—designed for accessibility and sustainable habits.

## Features

- **Weekly Workout Planner** – Mon–Sun schedule with focused routines (strength, mobility, breathing, cardio, recovery)
- **Exercise Details** – Images, instructions, timers, and rest intervals
- **YouTube Video Integration** – Day-specific guided workout videos
- **Progress Tracking** – Sessions, minutes, streaks, and performance trends

## Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Material Design 3**
- **Navigation Compose**
- **MVVM-ready** structure

## Project Structure

```
app/
├── data/
│   ├── model/          # WorkoutDay, Exercise, DayWorkout
│   └── repository/     # WorkoutRepository (to be implemented)
├── navigation/         # NavGraph, Screen routes
└── ui/
    ├── screens/        # WeeklyPlanScreen, ProgressScreen
    └── theme/          # GuidedFitnessTheme, Typography
```

## Getting Started

1. Open the project in **Android Studio** (Hedgehog or newer).
2. Sync Gradle and let it download dependencies.
3. Run on an emulator or physical device (API 26+).

If the Gradle wrapper is missing, run:
```bash
gradle wrapper --gradle-version=8.2
```

## Next Steps

- Implement `WorkoutRepository` with sample/cached data
- Add day detail screen with exercise list and timers
- Integrate YouTube Player for workout videos
- Add DataStore/Room for progress persistence and stats
- Add charts for progress visualization
