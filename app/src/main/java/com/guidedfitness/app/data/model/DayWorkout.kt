package com.guidedfitness.app.data.model

/**
 * Full workout plan for a specific day, including exercises and optional YouTube video.
 * customFocus overrides day.focus when user customizes.
 */
data class DayWorkout(
    val day: WorkoutDay,
    val exercises: List<Exercise>,
    val youtubeVideoId: String? = null,
    val totalDurationMinutes: Int,
    val customFocus: DayFocus? = null,
    val iconKey: String = ""
) {
    val focus: DayFocus get() = customFocus ?: day.focus
}
