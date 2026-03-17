package com.guidedfitness.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_videos",
    foreignKeys = [
        ForeignKey(
            entity = MonthlyDayEntity::class,
            parentColumns = ["dayId"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayId")]
)
data class MonthlyVideoEntity(
    @PrimaryKey val videoId: String,
    val dayId: String,
    val title: String,
    val thumbnailUrl: String?,
    val videoUrl: String,
    val orderIndex: Int,
    val updatedAt: Long
)

