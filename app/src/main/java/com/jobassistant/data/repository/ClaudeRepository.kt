package com.jobassistant.data.repository

import com.jobassistant.data.remote.model.CareerInsightsResult
import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.FitAnalysis

interface ClaudeRepository {
    suspend fun analyzeIntent(resumeText: String, userInterests: String): CareerProfile
    suspend fun evaluateFit(resumeText: String, jobDescription: String): FitAnalysis
    suspend fun parseEmail(subject: String, body: String): EmailAction
    suspend fun generateInsights(profileSummary: String, historySummary: String): CareerInsightsResult
}
