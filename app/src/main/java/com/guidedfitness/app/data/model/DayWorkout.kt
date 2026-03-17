package com.guidedfitness.app.data.model

/**
 * Full workout plan for a specific day, including exercises and optional YouTube video.
 */
data class DayWorkout(
    val day: WorkoutDay,
    val exercises: List<Exercise>,
    val youtubeVideoId: String? = null,
    val totalDurationMinutes: Int
)
