package com.guidedfitness.app.data.repository.local

import com.guidedfitness.app.data.local.dao.MonthlyPlanDao
import com.guidedfitness.app.data.local.entity.MonthlyDayEntity
import com.guidedfitness.app.data.local.entity.MonthlyVideoEntity
import com.guidedfitness.app.data.local.model.MonthlyDayWithVideos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class MonthlyVideo(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val videoUrl: String
)

data class MonthlyDay(
    val dayIndex: Int,
    val title: String,
    val videos: List<MonthlyVideo>
)

class LocalMonthlyPlanRepository(
    private val userId: String,
    private val dao: MonthlyPlanDao
) {
    fun observeMonthlyPlan(): Flow<List<MonthlyDay>> =
        dao.observeMonthlyPlanWithVideos(userId).map { list -> list.map { it.toDomain() } }

    fun observeDay(dayIndex: Int): Flow<MonthlyDay?> =
        dao.observeDayWithVideos(dayId(dayIndex)).map { it?.toDomain() }

    suspend fun ensureSeeded(days: Int = 30) {
        val existing = dao.getDays(userId)
        if (existing.size >= days) return
        val now = System.currentTimeMillis()
        for (i in 1..days) {
            dao.upsertDay(
                MonthlyDayEntity(
                    dayId = dayId(i),
                    userId = userId,
                    dayIndex = i,
                    title = "Day $i",
                    updatedAt = now
                )
            )
        }
    }

    suspend fun replaceDayVideos(dayIndex: Int, videos: List<MonthlyVideo>) {
        val now = System.currentTimeMillis()
        val id = dayId(dayIndex)
        dao.upsertDay(
            MonthlyDayEntity(
                dayId = id,
                userId = userId,
                dayIndex = dayIndex,
                title = "Day $dayIndex",
                updatedAt = now
            )
        )
        dao.deleteVideosForDay(id)
        dao.upsertVideos(
            videos.mapIndexed { idx, v ->
                MonthlyVideoEntity(
                    videoId = v.id.ifBlank { UUID.randomUUID().toString() },
                    dayId = id,
                    title = v.title,
                    thumbnailUrl = v.thumbnailUrl,
                    videoUrl = v.videoUrl,
                    orderIndex = idx,
                    updatedAt = now
                )
            }
        )
    }

    suspend fun addVideo(dayIndex: Int, video: MonthlyVideo) {
        val now = System.currentTimeMillis()
        val id = dayId(dayIndex)
        dao.upsertDay(
            MonthlyDayEntity(
                dayId = id,
                userId = userId,
                dayIndex = dayIndex,
                title = "Day $dayIndex",
                updatedAt = now
            )
        )
        val nextOrder = dao.getVideosForDay(id).size
        dao.upsertVideos(
            listOf(
                MonthlyVideoEntity(
                    videoId = video.id.ifBlank { UUID.randomUUID().toString() },
                    dayId = id,
                    title = video.title,
                    thumbnailUrl = video.thumbnailUrl,
                    videoUrl = video.videoUrl,
                    orderIndex = nextOrder,
                    updatedAt = now
                )
            )
        )
    }

    suspend fun removeVideo(videoId: String) {
        dao.deleteVideo(videoId)
    }

    suspend fun reorderDayVideos(dayIndex: Int, orderedVideoIds: List<String>) {
        val now = System.currentTimeMillis()
        orderedVideoIds.forEachIndexed { idx, id ->
            dao.updateVideoOrder(id, idx, now)
        }
    }

    private fun dayId(dayIndex: Int) = "${userId}_${dayIndex}"

    private fun MonthlyDayWithVideos.toDomain(): MonthlyDay =
        MonthlyDay(
            dayIndex = day.dayIndex,
            title = day.title,
            videos = videos.sortedBy { it.orderIndex }.map { v ->
                MonthlyVideo(
                    id = v.videoId,
                    title = v.title,
                    thumbnailUrl = v.thumbnailUrl,
                    videoUrl = v.videoUrl
                )
            }
        )
}

