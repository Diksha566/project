package com.guidedfitness.app.data.model

/**
 * Represents a single exercise within a daily workout.
 */
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val durationSeconds: Int,
    val restSeconds: Int,
    val imageResId: Int? = null,
    val imageUrl: String? = null,
    val youtubeLink: String? = null
)
