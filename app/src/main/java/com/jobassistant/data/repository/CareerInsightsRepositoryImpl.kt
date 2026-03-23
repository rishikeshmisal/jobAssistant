package com.jobassistant.data.repository

import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.mapper.toDomain
import com.jobassistant.data.db.mapper.toEntity
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.repository.CareerInsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CareerInsightsRepositoryImpl @Inject constructor(
    private val dao: CareerInsightsDao
) : CareerInsightsRepository {

    override fun getLatestAsFlow(): Flow<CareerInsights?> =
        dao.getLatestAsFlow().map { it?.toDomain() }

    override suspend fun save(insights: CareerInsights) =
        dao.upsert(insights.toEntity())
}
