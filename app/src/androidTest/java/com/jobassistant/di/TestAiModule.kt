package com.jobassistant.di

import com.jobassistant.data.remote.model.CareerInsightsResult
import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.EmailActionType
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.ClaudeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces [AiModule] in instrumented tests.
 *
 * Provides a [FakeClaudeRepository] that returns deterministic responses so
 * [GmailSyncWorkerIntegrationTest] can verify end-to-end behaviour without
 * real Gemini API calls.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AiModule::class]
)
object TestAiModule {

    @Provides
    @Singleton
    fun provideClaudeRepository(): ClaudeRepository = FakeClaudeRepository()
}

class FakeClaudeRepository : ClaudeRepository {

    override suspend fun analyzeIntent(resumeText: String, userInterests: String) = CareerProfile(
        currentLevel = "Mid-level",
        targetRoles = listOf("Android Engineer"),
        skillGaps = listOf("Kotlin Multiplatform"),
        recommendedFocusAreas = listOf("Compose"),
        goalMap = "Become a senior Android engineer."
    )

    override suspend fun evaluateFit(resumeText: String, jobDescription: String) = FitAnalysis(
        score = 80,
        pros = listOf("Kotlin experience"),
        cons = listOf("No team lead experience"),
        missingSkills = listOf("Docker")
    )

    override suspend fun parseEmail(subject: String, body: String) = EmailAction(
        actionType = EmailActionType.APPLIED,
        targetCompany = "Test Corp",
        roleTitle = "QA Engineer"
    )

    override suspend fun generateInsights(profileSummary: String, historySummary: String) =
        CareerInsightsResult(
            identifiedGaps = listOf("Leadership skills"),
            recommendedActions = listOf("Take a lead role on a project"),
            marketFeedbackSummary = "Strong technical candidate"
        )
}
