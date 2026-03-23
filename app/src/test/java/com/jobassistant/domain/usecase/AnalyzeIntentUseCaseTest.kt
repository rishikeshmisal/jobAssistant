package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.ClaudeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzeIntentUseCaseTest {

    private lateinit var claudeRepository: ClaudeRepository
    private lateinit var useCase: AnalyzeIntentUseCase

    private val fakeProfile = CareerProfile(
        currentLevel = "Mid-level",
        targetRoles = listOf("Senior Android Engineer"),
        skillGaps = listOf("Kotlin Multiplatform"),
        recommendedFocusAreas = listOf("Compose", "Architecture"),
        goalMap = "Become a senior Android engineer at a top tech company."
    )

    @Before
    fun setUp() {
        claudeRepository = mockk()
        useCase = AnalyzeIntentUseCase(claudeRepository)
    }

    @Test
    fun invoke_withNonEmptyResumeText_callsClaude() = runTest {
        coEvery { claudeRepository.analyzeIntent(any(), any()) } returns fakeProfile

        useCase("my resume text", "become a senior engineer")

        coVerify(exactly = 1) { claudeRepository.analyzeIntent(any(), any()) }
    }

    @Test
    fun invoke_withEmptyResumeText_stillCallsClaude() = runTest {
        // AnalyzeIntentUseCase itself does not gate on empty — gating is in ProfileViewModel
        coEvery { claudeRepository.analyzeIntent(any(), any()) } returns fakeProfile

        useCase("", "some goal")

        coVerify(exactly = 1) { claudeRepository.analyzeIntent("", "some goal") }
    }

    @Test
    fun invoke_returnsSuccessWithCareerProfile() = runTest {
        coEvery { claudeRepository.analyzeIntent(any(), any()) } returns fakeProfile

        val result = useCase("resume", "interests")

        assertTrue(result is ClaudeResult.Success)
        assertEquals(fakeProfile, (result as ClaudeResult.Success).data)
    }

    @Test
    fun invoke_goalMapIsCorrectlyReturned() = runTest {
        coEvery { claudeRepository.analyzeIntent(any(), any()) } returns fakeProfile

        val result = useCase("resume", "interests")

        val profile = (result as ClaudeResult.Success).data
        assertEquals("Become a senior Android engineer at a top tech company.", profile.goalMap)
    }

    @Test
    fun invoke_passesResumeTextAndInterestsToRepository() = runTest {
        coEvery { claudeRepository.analyzeIntent(any(), any()) } returns fakeProfile

        useCase("my resume", "my interests")

        coVerify { claudeRepository.analyzeIntent("my resume", "my interests") }
    }

    @Test
    fun invoke_repositoryThrows_returnsError() = runTest {
        coEvery { claudeRepository.analyzeIntent(any(), any()) } throws
                com.jobassistant.data.remote.model.ClaudeParseException("No tool_use block")

        val result = useCase("resume", "interests")

        assertTrue(result is ClaudeResult.Error)
    }
}
