package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.safeClaudeCall
import com.jobassistant.data.repository.ClaudeRepository
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.repository.CareerInsightsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

private const val CACHE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000  // 7 days

class GenerateInsightsUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository,
    private val careerInsightsRepository: CareerInsightsRepository
) {
    suspend operator fun invoke(
        profileSummary: String,
        historySummary: String
    ): ClaudeResult<CareerInsights> {
        val cached = careerInsightsRepository.getLatestAsFlow().firstOrNull()
        val cacheAge = System.currentTimeMillis() - (cached?.generatedDate ?: 0L)

        if (cached != null && cacheAge < CACHE_MAX_AGE_MS) {
            return ClaudeResult.Success(cached)
        }

        return safeClaudeCall {
            val result = claudeRepository.generateInsights(profileSummary, historySummary)
            val insights = CareerInsights(
                identifiedGaps = result.identifiedGaps,
                recommendedActions = result.recommendedActions,
                summaryAnalysis = result.marketFeedbackSummary
            )
            careerInsightsRepository.save(insights)
            insights
        }
    }
}
