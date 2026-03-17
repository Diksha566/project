package com.guidedfitness.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_logs",
    indices = [Index("userId"), Index("dateEpochDay")]
)
data class ProgressLogEntity(
    @PrimaryKey val logId: String,
    val userId: String,
    val dateEpochDay: Long,
    val day: String, // WorkoutDay.name
    val focus: String, // DayFocus.name
    val minutes: Int,
    val createdAt: Long
)

