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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class JobDetailViewModelTest {

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
        companyName = "TestCo",
        roleTitle = "Android Engineer",
        status = ApplicationStatus.APPLIED,
        fitScore = 72,
        notes = "great job description",
        location = "NYC",
        salaryRange = "100k-120k",
        jobDescription = "Kotlin Android job description"
    )

    private val fakeProfile = UserProfile(
        userId = "u1", fullName = "Test User",
        resumeText = "Kotlin and Android expert",
        keywords = emptyList(), careerGoal = "Senior Engineer",
        targetSalaryMin = 0, targetSalaryMax = 0
    )

    private fun createViewModel(): JobDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to jobId.toString()))
        return JobDetailViewModel(
            savedStateHandle,
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

    @Test
    fun `init loads job from repository by ID`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.job)
        assertEquals("TestCo", vm.uiState.value.job?.companyName)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init populates editable field flows from loaded job`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("great job description", vm.notes.value)
        assertEquals("NYC", vm.location.value)
        assertEquals("100k-120k", vm.salaryRange.value)
        assertEquals(ApplicationStatus.APPLIED, vm.status.value)
    }

    @Test
    fun `init populates jobDescription flow from loaded job`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Kotlin Android job description", vm.jobDescription.value)
    }

    @Test
    fun `analyzeFromPaste calls EvaluateFitUseCase with resume and supplied text`() = runTest {
        val fakeFit = FitAnalysis(score = 88, pros = listOf("Kotlin"), cons = emptyList(), missingSkills = emptyList())
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        val vm = createViewModel()
        advanceUntilIdle()
        vm.analyzeFromPaste("job posting text")
        advanceUntilIdle()

        coVerify { evaluateFitUseCase(fakeProfile.resumeText, "job posting text") }
    }

    @Test
    fun `analyzeFromPaste updates uiState fitAnalysis on success`() = runTest {
        val fakeFit = FitAnalysis(score = 90, pros = listOf("pro1"), cons = listOf("con1"), missingSkills = listOf("skill1"))
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFit)

        val vm = createViewModel()
        advanceUntilIdle()
        assertNull(vm.uiState.value.fitAnalysis)

        vm.analyzeFromPaste("job description")
        advanceUntilIdle()

        assertEquals(fakeFit, vm.uiState.value.fitAnalysis)
        assertFalse(vm.uiState.value.isAnalyzing)
    }

    @Test
    fun `analyzeFromPaste sets error on ClaudeResult Error`() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Error("Claude timeout", isRetryable = true)

        val vm = createViewModel()
        advanceUntilIdle()
        vm.analyzeFromPaste("some jd")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        assertNull(vm.uiState.value.fitAnalysis)
    }

    @Test
    fun `saveChanges calls repository save with updated fields`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.notes.value = "updated notes"
        vm.location.value = "Remote"
        vm.salaryRange.value = "130k"
        vm.status.value = ApplicationStatus.INTERVIEWING

        vm.saveChanges()
        advanceUntilIdle()

        coVerify {
            jobApplicationRepository.save(match { job ->
                job.notes == "updated notes" &&
                        job.location == "Remote" &&
                        job.salaryRange == "130k" &&
                        job.status == ApplicationStatus.INTERVIEWING
            })
        }
    }

    @Test
    fun `saveChanges includes jobDescription in saved job`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.jobDescription.value = "updated job description"

        vm.saveChanges()
        advanceUntilIdle()

        coVerify {
            jobApplicationRepository.save(match { it.jobDescription == "updated job description" })
        }
    }

    @Test
    fun `saveChanges sets saved flag on success`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.saveChanges()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun `unknown jobId results in error state`() = runTest {
        coEvery { getAllJobsUseCase() } returns flowOf(emptyList())
        val vm = createViewModel()
        advanceUntilIdle()

        assertNull(vm.uiState.value.job)
        assertNotNull(vm.uiState.value.error)
    }
}
