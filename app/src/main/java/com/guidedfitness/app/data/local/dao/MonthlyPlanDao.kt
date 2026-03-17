package com.guidedfitness.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.guidedfitness.app.data.local.entity.MonthlyDayEntity
import com.guidedfitness.app.data.local.entity.MonthlyVideoEntity
import com.guidedfitness.app.data.local.model.MonthlyDayWithVideos
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: MonthlyDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideos(videos: List<MonthlyVideoEntity>)

    @Query("DELETE FROM monthly_videos WHERE dayId = :dayId")
    suspend fun deleteVideosForDay(dayId: String)

    @Query("DELETE FROM monthly_videos WHERE videoId = :videoId")
    suspend fun deleteVideo(videoId: String)

    @Query("SELECT * FROM monthly_videos WHERE dayId = :dayId ORDER BY orderIndex ASC")
    suspend fun getVideosForDay(dayId: String): List<MonthlyVideoEntity>

    @Query("UPDATE monthly_videos SET orderIndex = :orderIndex, updatedAt = :updatedAt WHERE videoId = :videoId")
    suspend fun updateVideoOrder(videoId: String, orderIndex: Int, updatedAt: Long)

    @Query("SELECT * FROM monthly_days WHERE userId = :userId ORDER BY dayIndex ASC")
    suspend fun getDays(userId: String): List<MonthlyDayEntity>

    @Transaction
    @Query("SELECT * FROM monthly_days WHERE userId = :userId ORDER BY dayIndex ASC")
    fun observeMonthlyPlanWithVideos(userId: String): Flow<List<MonthlyDayWithVideos>>

    @Transaction
    @Query("SELECT * FROM monthly_days WHERE dayId = :dayId LIMIT 1")
    fun observeDayWithVideos(dayId: String): Flow<MonthlyDayWithVideos?>
}

