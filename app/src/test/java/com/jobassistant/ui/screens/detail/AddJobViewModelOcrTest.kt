package com.jobassistant.ui.screens.detail

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.EvaluateFitUseCase
import com.jobassistant.domain.usecase.FetchUrlUseCase
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.util.OcrProcessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Phase 8 - AddJobViewModelOcrTest
 *
 * Verifies the OCR path: that OcrProcessor output is passed unmodified to EvaluateFitUseCase.
 * Uses setOcrTextForTesting() to bypass the real OCR call (no ML Kit in unit test environment).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddJobViewModelOcrTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var evaluateFitUseCase: EvaluateFitUseCase
    private lateinit var saveJobUseCase: SaveJobApplicationUseCase
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var fetchUrlUseCase: FetchUrlUseCase
    private lateinit var jobRepository: JobApplicationRepository
    private lateinit var ocrProcessor: OcrProcessor
    private lateinit var viewModel: AddJobViewModel

    private val fakeProfile = UserProfile(
        userId = "u1",
        fullName = "Test User",
        resumeText = "Kotlin developer with 5 years experience",
        keywords = emptyList(),
        careerGoal = "Senior Engineer",
        targetSalaryMin = 0,
        targetSalaryMax = 0
    )

    private val fakeFit = FitAnalysis(
        score = 82,
        pros = listOf("Strong Kotlin"),
        cons = listOf("No iOS"),
        missingSkills = listOf("Swift")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        evaluateFitUseCase = mockk()
        saveJobUseCase = mockk(relaxed = true)
        userProfileDataStore = mockk()
        fetchUrlUseCase = mockk()
        jobRepository = mockk(relaxed = true)
        ocrProcessor = mockk()

        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)

        viewModel = AddJobViewModel(
            evaluateFitUseCase,
            saveJobUseCase,
            userProfileDataStore,
            fetchUrlUseCase,
            jobRepository,
            ocrProcessor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OCR text is passed directly to evaluateFit without modification`() = runTest {
        val ocrExtractedText = "Senior Android Engineer – 5+ years Kotlin, Compose, Hilt"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        // Simulate OCR extraction result being set
        viewModel.setOcrTextForTesting(ocrExtractedText)

        // Trigger analysis using the OCR text (same path the UI uses for Screenshot mode)
        viewModel.analyzeFit(ocrExtractedText)
        advanceUntilIdle()

        // Verify evaluateFitUseCase receives the exact OCR string, unmodified
        coVerify { evaluateFitUseCase(fakeProfile.resumeText, ocrExtractedText) }
    }

    @Test
    fun `analyzeFit with OCR text transitions to FitResult state`() = runTest {
        val ocrText = "Software Engineer role requiring Python and Spark"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        viewModel.setOcrTextForTesting(ocrText)
        viewModel.analyzeFit(ocrText)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.FitResult)
        assertEquals(82, (state as AddJobUiState.FitResult).analysis.score)
    }

    @Test
    fun `analyzeFit with OCR text emits Error state on Claude failure`() = runTest {
        val ocrText = "Data Scientist – ML, PyTorch"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Error("API timeout", true)

        viewModel.setOcrTextForTesting(ocrText)
        viewModel.analyzeFit(ocrText)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.Error)
        assertEquals("API timeout", (state as AddJobUiState.Error).message)
    }
}
