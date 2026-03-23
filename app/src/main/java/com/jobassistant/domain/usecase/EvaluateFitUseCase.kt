package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.remote.model.safeClaudeCall
import com.jobassistant.data.repository.ClaudeRepository
import javax.inject.Inject

class EvaluateFitUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository
) {
    suspend operator fun invoke(
        resumeText: String,
        jobDescription: String
    ): ClaudeResult<FitAnalysis> = safeClaudeCall {
        claudeRepository.evaluateFit(resumeText, jobDescription)
    }
}
