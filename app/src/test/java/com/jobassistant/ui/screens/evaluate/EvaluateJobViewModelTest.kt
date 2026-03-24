package com.jobassistant.ui.screens.evaluate

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.EvaluateFitUseCase
import com.jobassistant.domain.usecase.FetchUrlUseCase
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.domain.usecase.SaveResult
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class EvaluateJobViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var evaluateFitUseCase: EvaluateFitUseCase
    private lateinit var fetchUrlUseCase: FetchUrlUseCase
    private lateinit var ocrProcessor: OcrProcessor
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var saveJobApplicationUseCase: SaveJobApplicationUseCase
    private lateinit var jobApplicationRepository: JobApplicationRepository
    private lateinit var viewModel: EvaluateJobViewModel

    private val fakeProfile = UserProfile(
        resumeText = "Kotlin Android developer with 5 years experience",
        fullName = "Test User"
    )

    private val fakeFit = FitAnalysis(
        score = 78,
        pros = listOf("Strong Kotlin"),
        cons = listOf("No iOS"),
        missingSkills = listOf("Swift")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        evaluateFitUseCase = mockk()
        fetchUrlUseCase = mockk()
        ocrProcessor = mockk()
        userProfileDataStore = mockk()
        saveJobApplicationUseCase = mockk()
        jobApplicationRepository = mockk(relaxed = true)

        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel = EvaluateJobViewModel(
            evaluateFitUseCase,
            fetchUrlUseCase,
            ocrProcessor,
            userProfileDataStore,
            saveJobApplicationUseCase,
            jobApplicationRepository
        )
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // ── Test 1 ────────────────────────────────────────────────────────────────

    @Test
    fun `analyzeFromPaste calls EvaluateFitUseCase with resume text and supplied text`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        viewModel.analyzeFromPaste("Senior Android Engineer job description")
        advanceUntilIdle()

        coVerify { evaluateFitUseCase(fakeProfile.resumeText, "Senior Android Engineer job description") }
    }

    // ── Test 2 ────────────────────────────────────────────────────────────────

    @Test
    fun `analyzeFromPaste transitions Idle to Analyzing to Result`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        viewModel.analyzeFromPaste("job description text")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EvaluateJobUiState.Result)
        assertEquals(fakeFit, (state as EvaluateJobUiState.Result).analysis)
        assertEquals("job description text", state.jobDescription)
    }

    // ── Test 3 ────────────────────────────────────────────────────────────────

    @Test
    fun `analyzeFromUrl calls FetchUrlUseCase then EvaluateFitUseCase in sequence`() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns "stripped job description from page"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        viewModel.analyzeFromUrl("https://example.com/jobs/android-engineer")
        advanceUntilIdle()

        coVerify { fetchUrlUseCase("https://example.com/jobs/android-engineer") }
        coVerify { evaluateFitUseCase(fakeProfile.resumeText, "stripped job description from page") }
    }

    // ── Test 4 ────────────────────────────────────────────────────────────────

    @Test
    fun `analyzeFromPaste with blank text stays Idle`() = runTest {
        viewModel.analyzeFromPaste("   ")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is EvaluateJobUiState.Idle)
        coVerify(exactly = 0) { evaluateFitUseCase(any(), any()) }
    }

    // ── Test 5 ────────────────────────────────────────────────────────────────

    @Test
    fun `ClaudeResult Error transitions state to Error`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Error("API timeout", true)

        viewModel.analyzeFromPaste("job description")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EvaluateJobUiState.Error)
        assertEquals("API timeout", (state as EvaluateJobUiState.Error).message)
    }

    // ── Test 6 ────────────────────────────────────────────────────────────────

    @Test
    fun `saveJob calls SaveJobApplicationUseCase with fitScore jobDescription and APPLIED status`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        viewModel.analyzeFromPaste("job desc")
        advanceUntilIdle()

        viewModel.saveJob("Google", "SWE")
        advanceUntilIdle()

        coVerify {
            saveJobApplicationUseCase(match { job ->
                job.companyName == "Google" &&
                        job.roleTitle == "SWE" &&
                        job.fitScore == 78 &&
                        job.jobDescription == "job desc" &&
                        job.status == ApplicationStatus.APPLIED
            })
        }
        assertTrue(viewModel.uiState.value is EvaluateJobUiState.Saved)
    }

    // ── Test 7 ────────────────────────────────────────────────────────────────

    @Test
    fun `saveJob with blank names uses placeholder values`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        viewModel.analyzeFromPaste("jd")
        advanceUntilIdle()

        viewModel.saveJob("", "")
        advanceUntilIdle()

        coVerify {
            saveJobApplicationUseCase(match { job ->
                job.companyName == "Unknown Company" && job.roleTitle == "Unknown Role"
            })
        }
    }

    // ── Bonus: reset() ────────────────────────────────────────────────────────

    @Test
    fun `reset returns to Idle from any state`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Error("fail", false)
        viewModel.analyzeFromPaste("text")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is EvaluateJobUiState.Error)

        viewModel.reset()

        assertTrue(viewModel.uiState.value is EvaluateJobUiState.Idle)
    }
}
