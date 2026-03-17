package com.guidedfitness.app.data.model

/**
 * Represents a day in the weekly workout plan (Mon–Sun).
 */
enum class WorkoutDay(val displayName: String, val focus: DayFocus) {
    MONDAY("Monday", DayFocus.STRENGTH),
    TUESDAY("Tuesday", DayFocus.MOBILITY),
    WEDNESDAY("Wednesday", DayFocus.BREATHING),
    THURSDAY("Thursday", DayFocus.STRENGTH),
    FRIDAY("Friday", DayFocus.CARDIO),
    SATURDAY("Saturday", DayFocus.MOBILITY),
    SUNDAY("Sunday", DayFocus.RECOVERY)
}

enum class DayFocus(val displayName: String) {
    STRENGTH("Strength"),
    MOBILITY("Mobility"),
    BREATHING("Breathing"),
    CARDIO("Cardio"),
    RECOVERY("Recovery")
}
