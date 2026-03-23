package com.jobassistant.domain.repository

import com.jobassistant.domain.model.CareerInsights
import kotlinx.coroutines.flow.Flow

interface CareerInsightsRepository {
    fun getLatestAsFlow(): Flow<CareerInsights?>
    suspend fun save(insights: CareerInsights)
}
