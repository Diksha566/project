package com.guidedfitness.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_workouts")
data class DayWorkoutEntity(
    @PrimaryKey val workoutId: String, // "${userId}_${day}"
    val userId: String,
    val day: String, // WorkoutDay.name
    val focus: String, // DayFocus.name
    val iconKey: String,
    val youtubeVideoId: String?,
    val updatedAt: Long
)

