package com.guidedfitness.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guidedfitness.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE exerciseId = :exerciseId")
    suspend fun deleteById(exerciseId: String)

    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun observeForWorkout(workoutId: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE workoutId IN (:workoutIds) ORDER BY workoutId ASC, orderIndex ASC")
    suspend fun getForWorkouts(workoutIds: List<String>): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises WHERE workoutId = :workoutId")
    suspend fun countForWorkout(workoutId: String): Int
}

