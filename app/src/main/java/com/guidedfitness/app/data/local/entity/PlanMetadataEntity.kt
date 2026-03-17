package com.guidedfitness.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_metadata")
data class PlanMetadataEntity(
    @PrimaryKey val userId: String,
    val title: String,
    val description: String
)

