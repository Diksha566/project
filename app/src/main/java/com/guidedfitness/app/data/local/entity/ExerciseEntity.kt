package com.guidedfitness.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = DayWorkoutEntity::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class ExerciseEntity(
    @PrimaryKey val exerciseId: String,
    val workoutId: String,
    val name: String,
    val description: String,
    val durationSeconds: Int,
    val restSeconds: Int,
    val imageUrl: String?,
    val youtubeLink: String?,
    val orderIndex: Int,
    val updatedAt: Long
)

