package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.CareerInsightsResult
import com.jobassistant.data.repository.ClaudeRepository
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.repository.CareerInsightsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateInsightsUseCaseTest {

    private lateinit var claudeRepository: ClaudeRepository
    private lateinit var careerInsightsRepository: CareerInsightsRepository
    private lateinit var useCase: GenerateInsightsUseCase

    @Before
    fun setUp() {
        claudeRepository = mockk()
        careerInsightsRepository = mockk(relaxed = true)
        useCase = GenerateInsightsUseCase(claudeRepository, careerInsightsRepository)
    }

    @Test
    fun invoke_cacheFresh_claudeNotCalled() = runTest {
        val freshInsights = CareerInsights(
            generatedDate = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // 2 hours ago
            identifiedGaps = listOf("gap1"),
            recommendedActions = listOf("action1"),
            summaryAnalysis = "cached summary"
        )
        coEvery { careerInsightsRepository.getLatestAsFlow() } returns flowOf(freshInsights)

        val result = useCase("profile", "history")

        coVerify(exactly = 0) { claudeRepository.generateInsights(any(), any()) }
        assertTrue(result is com.jobassistant.data.remote.model.ClaudeResult.Success)
        assertEquals(freshInsights, (result as com.jobassistant.data.remote.model.ClaudeResult.Success).data)
    }

    @Test
    fun invoke_cacheStale_claudeIsCalled() = runTest {
        val staleInsights = CareerInsights(
            generatedDate = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000), // 8 days ago
            identifiedGaps = listOf("old gap"),
            summaryAnalysis = "old summary"
        )
        val freshResult = CareerInsightsResult(
            identifiedGaps = listOf("new gap"),
            recommendedActions = listOf("new action"),
            marketFeedbackSummary = "new summary"
        )
        coEvery { careerInsightsRepository.getLatestAsFlow() } returns flowOf(staleInsights)
        coEvery { claudeRepository.generateInsights(any(), any()) } returns freshResult

        val result = useCase("profile", "history")

        coVerify(exactly = 1) { claudeRepository.generateInsights(any(), any()) }
        assertTrue(result is com.jobassistant.data.remote.model.ClaudeResult.Success)
        val data = (result as com.jobassistant.data.remote.model.ClaudeResult.Success).data
        assertEquals(listOf("new gap"), data.identifiedGaps)
        assertEquals("new summary", data.summaryAnalysis)
    }

    @Test
    fun invoke_noCache_claudeIsCalled() = runTest {
        coEvery { careerInsightsRepository.getLatestAsFlow() } returns flowOf(null)
        val freshResult = CareerInsightsResult(
            identifiedGaps = listOf("gap"),
            recommendedActions = listOf("act"),
            marketFeedbackSummary = "summary"
        )
        coEvery { claudeRepository.generateInsights(any(), any()) } returns freshResult

        useCase("profile", "history")

        coVerify(exactly = 1) { claudeRepository.generateInsights(any(), any()) }
    }

    @Test
    fun invoke_staleCache_savedToRepository() = runTest {
        val staleInsights = CareerInsights(
            generatedDate = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000)
        )
        val apiResult = CareerInsightsResult(
            identifiedGaps = listOf("g"),
            recommendedActions = listOf("a"),
            marketFeedbackSummary = "s"
        )
        coEvery { careerInsightsRepository.getLatestAsFlow() } returns flowOf(staleInsights)
        coEvery { claudeRepository.generateInsights(any(), any()) } returns apiResult

        useCase("profile", "history")

        coVerify { careerInsightsRepository.save(any()) }
    }
}
