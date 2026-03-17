package com.guidedfitness.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guidedfitness.app.data.local.entity.ProgressLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ProgressLogEntity)

    @Query("SELECT * FROM progress_logs WHERE userId = :userId ORDER BY dateEpochDay ASC")
    fun observeAllLogs(userId: String): Flow<List<ProgressLogEntity>>

    @Query("SELECT COUNT(*) FROM progress_logs WHERE userId = :userId")
    suspend fun totalSessions(userId: String): Int

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM progress_logs WHERE userId = :userId")
    suspend fun totalMinutes(userId: String): Int

    @Query("SELECT DISTINCT dateEpochDay FROM progress_logs WHERE userId = :userId ORDER BY dateEpochDay ASC")
    suspend fun distinctWorkoutDays(userId: String): List<Long>

    @Query("SELECT * FROM progress_logs WHERE userId = :userId AND dateEpochDay BETWEEN :fromDay AND :toDay ORDER BY dateEpochDay ASC")
    suspend fun logsBetween(userId: String, fromDay: Long, toDay: Long): List<ProgressLogEntity>
}
