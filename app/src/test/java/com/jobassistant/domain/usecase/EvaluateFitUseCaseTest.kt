package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.ClaudeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvaluateFitUseCaseTest {

    private lateinit var claudeRepository: ClaudeRepository
    private lateinit var useCase: EvaluateFitUseCase

    private val fakeFitAnalysis = FitAnalysis(
        score = 75,
        pros = listOf("Kotlin skills"),
        cons = listOf("No iOS experience"),
        missingSkills = listOf("Swift")
    )

    @Before
    fun setUp() {
        claudeRepository = mockk()
        useCase = EvaluateFitUseCase(claudeRepository)
    }

    @Test
    fun invoke_passesResumeAndJobDescriptionToRepository() = runTest {
        coEvery { claudeRepository.evaluateFit(any(), any()) } returns fakeFitAnalysis

        useCase("my resume", "job posting")

        coVerify { claudeRepository.evaluateFit("my resume", "job posting") }
    }

    @Test
    fun invoke_returnsSuccessWithFitAnalysis() = runTest {
        coEvery { claudeRepository.evaluateFit(any(), any()) } returns fakeFitAnalysis

        val result = useCase("resume", "jd")

        assertTrue(result is com.jobassistant.data.remote.model.ClaudeResult.Success)
        assertEquals(fakeFitAnalysis, (result as com.jobassistant.data.remote.model.ClaudeResult.Success).data)
    }

    @Test
    fun invoke_repositoryThrows_returnsError() = runTest {
        coEvery { claudeRepository.evaluateFit(any(), any()) } throws
                com.jobassistant.data.remote.model.ClaudeParseException("No tool_use block")

        val result = useCase("resume", "jd")

        assertTrue(result is com.jobassistant.data.remote.model.ClaudeResult.Error)
    }

    @Test
    fun invoke_returnsUnchangedAnalysis() = runTest {
        val expected = FitAnalysis(score = 90, pros = listOf("a"), cons = listOf("b"), missingSkills = listOf("c"))
        coEvery { claudeRepository.evaluateFit(any(), any()) } returns expected

        val result = useCase("r", "j")

        assertEquals(expected, (result as com.jobassistant.data.remote.model.ClaudeResult.Success).data)
    }
}
