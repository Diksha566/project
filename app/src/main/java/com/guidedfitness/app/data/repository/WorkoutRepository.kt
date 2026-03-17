package com.guidedfitness.app.data.repository

import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.WorkoutDay
import kotlinx.coroutines.flow.Flow

/**
 * Repository for workout data. Will be extended with local persistence and progress tracking.
 */
interface WorkoutRepository {
    fun getWeeklyPlan(): Flow<List<DayWorkout>>
    fun getDayWorkout(day: WorkoutDay): Flow<DayWorkout?>
}
