package com.guidedfitness.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.guidedfitness.app.data.local.entity.MonthlyDayEntity
import com.guidedfitness.app.data.local.entity.MonthlyVideoEntity

data class MonthlyDayWithVideos(
    @Embedded val day: MonthlyDayEntity,
    @Relation(
        parentColumn = "dayId",
        entityColumn = "dayId",
        entity = MonthlyVideoEntity::class
    )
    val videos: List<MonthlyVideoEntity>
)

