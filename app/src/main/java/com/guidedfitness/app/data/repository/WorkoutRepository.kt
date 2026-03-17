package com.guidedfitness.app.data.repository

import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getWeeklyPlan(): Flow<List<DayWorkout>>
    fun getDayWorkout(day: WorkoutDay): Flow<DayWorkout?>
    suspend fun updateDayWorkout(dayWorkout: DayWorkout)
    suspend fun addExercise(day: WorkoutDay, exercise: Exercise)
    suspend fun removeExercise(day: WorkoutDay, exerciseId: String)
    suspend fun updateDayFocus(day: WorkoutDay, focus: DayFocus)
    suspend fun setYoutubeVideoId(day: WorkoutDay, videoId: String?)
}
