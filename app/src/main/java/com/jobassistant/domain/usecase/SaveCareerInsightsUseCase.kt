package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.repository.CareerInsightsRepository
import javax.inject.Inject

class SaveCareerInsightsUseCase @Inject constructor(
    private val repository: CareerInsightsRepository
) {
    suspend operator fun invoke(insights: CareerInsights) = repository.save(insights)
}
