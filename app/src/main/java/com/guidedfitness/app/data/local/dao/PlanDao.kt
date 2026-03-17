package com.guidedfitness.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.PlanMetadataEntity
import com.guidedfitness.app.data.local.model.DayWorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: PlanMetadataEntity)

    @Query("SELECT * FROM plan_metadata WHERE userId = :userId LIMIT 1")
    fun observeMetadata(userId: String): Flow<PlanMetadataEntity?>

    @Query("SELECT * FROM plan_metadata WHERE userId = :userId LIMIT 1")
    suspend fun getMetadata(userId: String): PlanMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayWorkout(dayWorkout: DayWorkoutEntity)

    @Query("SELECT * FROM day_workouts WHERE userId = :userId ORDER BY day ASC")
    fun observeDayWorkouts(userId: String): Flow<List<DayWorkoutEntity>>

    @Query("SELECT * FROM day_workouts WHERE userId = :userId ORDER BY day ASC")
    suspend fun getDayWorkouts(userId: String): List<DayWorkoutEntity>

    @Query("SELECT * FROM day_workouts WHERE workoutId = :workoutId LIMIT 1")
    fun observeDayWorkout(workoutId: String): Flow<DayWorkoutEntity?>

    @Query("SELECT * FROM day_workouts WHERE workoutId = :workoutId LIMIT 1")
    suspend fun getDayWorkout(workoutId: String): DayWorkoutEntity?

    @Transaction
    @Query("SELECT * FROM day_workouts WHERE userId = :userId ORDER BY day ASC")
    fun observeWeeklyPlanWithExercises(userId: String): Flow<List<DayWorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM day_workouts WHERE workoutId = :workoutId LIMIT 1")
    fun observeDayWorkoutWithExercises(workoutId: String): Flow<DayWorkoutWithExercises?>

    @Query("SELECT COUNT(*) FROM day_workouts WHERE userId = :userId")
    suspend fun countDayWorkouts(userId: String): Int
}
