package com.guidedfitness.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.ExerciseEntity

data class DayWorkoutWithExercises(
    @Embedded val workout: DayWorkoutEntity,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId",
        entity = ExerciseEntity::class
    )
    val exercises: List<ExerciseEntity>
)
