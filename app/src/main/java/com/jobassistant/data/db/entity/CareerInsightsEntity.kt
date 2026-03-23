package com.jobassistant.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "career_insights")
data class CareerInsightsEntity(
    @PrimaryKey val id: String,
    val generatedDate: Long,
    val identifiedGaps: List<String>,
    val recommendedActions: List<String>,
    val summaryAnalysis: String
)
