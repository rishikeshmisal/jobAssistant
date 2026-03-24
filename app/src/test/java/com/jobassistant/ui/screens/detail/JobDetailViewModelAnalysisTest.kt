package com.jobassistant.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.EvaluateFitUseCase
import com.jobassistant.domain.usecase.FetchUrlUseCase
import com.jobassistant.domain.usecase.GetAllJobsUseCase
import com.jobassistant.domain.usecase.UpdateJobStatusUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Tests for the analysis paths (analyzeFromPaste, analyzeFromUrl, analyzeFromScreenshot)
 * that were moved from AddJobViewModel to JobDetailViewModel in Phase 12.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JobDetailViewModelAnalysisTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getAllJobsUseCase: GetAllJobsUseCase
    private lateinit var jobApplicationRepository: JobApplicationRepository
    private lateinit var updateJobStatusUseCase: UpdateJobStatusUseCase
    private lateinit var evaluateFitUseCase: EvaluateFitUseCase
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var fetchUrlUseCase: FetchUrlUseCase
    private lateinit var ocrProcessor: OcrProcessor

    private val jobId = UUID.randomUUID()
    private val testJob = JobApplication(
        id = jobId,
        companyName = "DeepMind",
        roleTitle = "ML Engineer",
        status = ApplicationStatus.INTERESTED,
        fitScore = null,
        jobDescription = ""
    )

    private val fakeProfile = UserProfile(
        userId = "u1",
        fullName = "Test User",
        resumeText = "Kotlin and Android expert with 5 years experience",
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

    private fun createViewModel(): JobDetailViewModel {
        val handle = SavedStateHandle(mapOf("jobId" to jobId.toString()))
        return JobDetailViewModel(
            handle,
            getAllJobsUseCase,
            jobApplicationRepository,
            updateJobStatusUseCase,
            evaluateFitUseCase,
            userProfileDataStore,
            fetchUrlUseCase,
            ocrProcessor
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllJobsUseCase = mockk()
        jobApplicationRepository = mockk(relaxed = true)
        updateJobStatusUseCase = mockk(relaxed = true)
        evaluateFitUseCase = mockk()
        userProfileDataStore = mockk()
        fetchUrlUseCase = mockk()
        ocrProcessor = mockk()

        coEvery { getAllJobsUseCase() } returns flowOf(listOf(testJob))
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // ── analyzeFromPaste ──────────────────────────────────────────────────────

    @Test
    fun `analyzeFromPaste calls EvaluateFitUseCase with resumeText and supplied text`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromPaste("Senior Android Engineer – Kotlin, Compose, Hilt required")
        advanceUntilIdle()

        coVerify {
            evaluateFitUseCase(
                fakeProfile.resumeText,
                "Senior Android Engineer – Kotlin, Compose, Hilt required"
            )
        }
    }

    @Test
    fun `analyzeFromPaste updates uiState fitAnalysis on success`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        val vm = createViewModel()
        advanceUntilIdle()

        assertNull(vm.uiState.value.fitAnalysis)
        vm.analyzeFromPaste("job description text")
        advanceUntilIdle()

        assertEquals(fakeFit, vm.uiState.value.fitAnalysis)
        assertFalse(vm.uiState.value.isAnalyzing)
    }

    @Test
    fun `analyzeFromPaste auto-saves fit score and jobDescription to Room`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        val vm = createViewModel()
        advanceUntilIdle()

        val text = "Data Scientist role requiring Python and SQL"
        vm.analyzeFromPaste(text)
        advanceUntilIdle()

        coVerify {
            jobApplicationRepository.save(match { job ->
                job.fitScore == 82 && job.jobDescription == text
            })
        }
    }

    @Test
    fun `analyzeFromPaste emits Error state on Claude failure`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Error("API timeout", true)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromPaste("some job description")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        assertEquals("API timeout", vm.uiState.value.error)
        assertNull(vm.uiState.value.fitAnalysis)
    }

    @Test
    fun `analyzeFromPaste ignores blank text`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromPaste("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { evaluateFitUseCase(any(), any()) }
    }

    // ── analyzeFromUrl ────────────────────────────────────────────────────────

    @Test
    fun `analyzeFromUrl calls FetchUrlUseCase then EvaluateFitUseCase in sequence`() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns "stripped job description from web page"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromUrl("https://example.com/jobs/123")
        advanceUntilIdle()

        coVerify { fetchUrlUseCase("https://example.com/jobs/123") }
        coVerify { evaluateFitUseCase(fakeProfile.resumeText, "stripped job description from web page") }
    }

    @Test
    fun `analyzeFromUrl emits error when fetch returns null`() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns null
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromUrl("https://example.com/bad-url")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        coVerify(exactly = 0) { evaluateFitUseCase(any(), any()) }
    }

    @Test
    fun `analyzeFromUrl emits error when fetch returns blank`() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns "   "
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromUrl("https://example.com/blank")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        coVerify(exactly = 0) { evaluateFitUseCase(any(), any()) }
    }

    @Test
    fun `analyzeFromUrl on success updates fitAnalysis and saves to Room`() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns "job description from url"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.analyzeFromUrl("https://example.com/job")
        advanceUntilIdle()

        assertEquals(fakeFit, vm.uiState.value.fitAnalysis)
        coVerify { jobApplicationRepository.save(match { it.fitScore == 82 }) }
    }

    // ── OCR text state ────────────────────────────────────────────────────────

    @Test
    fun `ocrText starts empty`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("", vm.ocrText.value)
    }

    @Test
    fun `jobDescription pre-fills from saved job on load`() = runTest {
        val jobWithDesc = testJob.copy(jobDescription = "Existing job description")
        coEvery { getAllJobsUseCase() } returns flowOf(listOf(jobWithDesc))
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Existing job description", vm.jobDescription.value)
    }
}
