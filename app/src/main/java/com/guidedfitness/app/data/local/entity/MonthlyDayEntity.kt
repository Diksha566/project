package com.guidedfitness.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_days",
    indices = [Index(value = ["userId", "dayIndex"], unique = true)]
)
data class MonthlyDayEntity(
    @PrimaryKey val dayId: String, // "${userId}_${dayIndex}"
    val userId: String,
    val dayIndex: Int, // 1..30
    val title: String, // "Day 1"
    val updatedAt: Long
)

