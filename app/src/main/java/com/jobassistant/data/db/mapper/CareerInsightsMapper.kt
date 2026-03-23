package com.jobassistant.data.db.mapper

import com.jobassistant.data.db.entity.CareerInsightsEntity
import com.jobassistant.domain.model.CareerInsights
import java.util.UUID

fun CareerInsightsEntity.toDomain(): CareerInsights = CareerInsights(
    id = UUID.fromString(id),
    generatedDate = generatedDate,
    identifiedGaps = identifiedGaps,
    recommendedActions = recommendedActions,
    summaryAnalysis = summaryAnalysis
)

fun CareerInsights.toEntity(): CareerInsightsEntity = CareerInsightsEntity(
    id = id.toString(),
    generatedDate = generatedDate,
    identifiedGaps = identifiedGaps,
    recommendedActions = recommendedActions,
    summaryAnalysis = summaryAnalysis
)
