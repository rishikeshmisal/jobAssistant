package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.repository.CareerInsightsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCareerInsightsUseCase @Inject constructor(
    private val repository: CareerInsightsRepository
) {
    operator fun invoke(): Flow<CareerInsights?> = repository.getLatestAsFlow()
}
