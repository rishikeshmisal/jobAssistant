package com.jobassistant.ui.screens.detail

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
class AddJobViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var evaluateFitUseCase: EvaluateFitUseCase
    private lateinit var saveJobApplicationUseCase: SaveJobApplicationUseCase
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var fetchUrlUseCase: FetchUrlUseCase
    private lateinit var jobApplicationRepository: JobApplicationRepository
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

    private val fakeFitAnalysis = FitAnalysis(
        score = 85,
        pros = listOf("Kotlin experience"),
        cons = listOf("No iOS"),
        missingSkills = listOf("Swift")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        evaluateFitUseCase = mockk()
        saveJobApplicationUseCase = mockk(relaxed = true)
        userProfileDataStore = mockk()
        fetchUrlUseCase = mockk()
        jobApplicationRepository = mockk(relaxed = true)
        ocrProcessor = mockk(relaxed = true)
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)
        viewModel = AddJobViewModel(
            evaluateFitUseCase,
            saveJobApplicationUseCase,
            userProfileDataStore,
            fetchUrlUseCase,
            jobApplicationRepository,
            ocrProcessor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── analyzeFit (paste mode) ──────────────────────────────────────────────

    @Test
    fun initialState_isIdle() {
        assertTrue(viewModel.uiState.value is AddJobUiState.Idle)
    }

    @Test
    fun analyzeFit_transitionsToAnalyzing_thenFitResult() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFitAnalysis)

        viewModel.analyzeFit("job description text")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.FitResult)
        assertEquals(fakeFitAnalysis, (state as AddJobUiState.FitResult).analysis)
    }

    @Test
    fun analyzeFit_usesResumeTextFromDataStore() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFitAnalysis)

        viewModel.analyzeFit("some jd")
        advanceUntilIdle()

        coVerify { evaluateFitUseCase(fakeProfile.resumeText, "some jd") }
    }

    @Test
    fun analyzeFit_onError_emitsErrorState() = runTest {
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Error("Network failure", true)

        viewModel.analyzeFit("jd")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.Error)
        assertEquals("Network failure", (state as AddJobUiState.Error).message)
    }

    // ── fetchAndAnalyzeUrl ───────────────────────────────────────────────────

    @Test
    fun fetchAndAnalyzeUrl_withStrippedText_callsEvaluateFit() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns "stripped job description"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFitAnalysis)

        viewModel.fetchAndAnalyzeUrl("https://example.com/job")
        advanceUntilIdle()

        coVerify { evaluateFitUseCase(fakeProfile.resumeText, "stripped job description") }
    }

    @Test
    fun fetchAndAnalyzeUrl_onNullFetch_emitsError() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns null

        viewModel.fetchAndAnalyzeUrl("https://example.com/job")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AddJobUiState.Error)
    }

    @Test
    fun fetchAndAnalyzeUrl_onSuccess_emitsFitResult() = runTest {
        coEvery { fetchUrlUseCase(any()) } returns "job description from url"
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fakeFitAnalysis)

        viewModel.fetchAndAnalyzeUrl("https://example.com/job")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AddJobUiState.FitResult)
    }

    // ── saveJob ──────────────────────────────────────────────────────────────

    @Test
    fun saveJob_transitionsToSaving_thenSaved() = runTest {
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel.saveJob("Acme", "Engineer", "jd text", "Remote", "100k", 85)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AddJobUiState.Saved)
    }

    @Test
    fun saveJob_callsSaveUseCaseWithCorrectFields() = runTest {
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel.saveJob("Acme Corp", "Android Engineer", "jd", "NYC", "120k", 90)
        advanceUntilIdle()

        coVerify {
            saveJobApplicationUseCase(match { job ->
                job.companyName == "Acme Corp" &&
                        job.roleTitle == "Android Engineer" &&
                        job.fitScore == 90 &&
                        job.location == "NYC" &&
                        job.salaryRange == "120k"
            })
        }
    }

    // ── duplicate detection ──────────────────────────────────────────────────

    @Test
    fun saveJob_onDuplicate_emitsDuplicateState() = runTest {
        val existing = JobApplication(
            id = UUID.randomUUID(),
            companyName = "Google",
            roleTitle = "Android Engineer",
            status = ApplicationStatus.APPLIED
        )
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)

        viewModel.saveJob("Google", "Android Engineer", "jd", "NYC", "120k", 80)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.Duplicate)
        assertEquals("Google", (state as AddJobUiState.Duplicate).companyName)
        assertEquals("Android Engineer", state.roleTitle)
    }

    @Test
    fun dismissDuplicate_resetsStateToIdle() = runTest {
        val existing = JobApplication(id = UUID.randomUUID(), companyName = "Google", roleTitle = "Eng")
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)
        viewModel.saveJob("Google", "Eng", "jd", "", "", 80)
        advanceUntilIdle()

        viewModel.dismissDuplicate()

        assertTrue(viewModel.uiState.value is AddJobUiState.Idle)
    }

    @Test
    fun saveJobForce_callsRepositoryDirectly() = runTest {
        val existing = JobApplication(id = UUID.randomUUID(), companyName = "Google", roleTitle = "Eng")
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)
        viewModel.saveJob("Google", "Eng", "jd", "", "", 80)
        advanceUntilIdle()

        viewModel.saveJobForce()
        advanceUntilIdle()

        coVerify { jobApplicationRepository.save(any()) }
        assertTrue(viewModel.uiState.value is AddJobUiState.Saved)
    }
}
