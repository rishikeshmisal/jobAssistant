package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.safeClaudeCall
import com.jobassistant.data.repository.ClaudeRepository
import javax.inject.Inject

class AnalyzeIntentUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository
) {
    suspend operator fun invoke(
        resumeText: String,
        userInterests: String
    ): ClaudeResult<CareerProfile> = safeClaudeCall {
        claudeRepository.analyzeIntent(resumeText, userInterests)
    }
}
