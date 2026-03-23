package com.jobassistant.domain.model

import java.util.UUID

data class CareerInsights(
    val id: UUID = UUID.randomUUID(),
    val generatedDate: Long = System.currentTimeMillis(),
    val identifiedGaps: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList(),
    val summaryAnalysis: String = ""
)
