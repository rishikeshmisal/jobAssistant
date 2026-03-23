package com.jobassistant.ui.screens.profile

import android.content.Context
import android.net.Uri
import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.GmailTokenManager
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.AnalyzeIntentUseCase
import com.jobassistant.util.ExportManager
import com.jobassistant.util.PdfTextExtractor
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var analyzeIntentUseCase: AnalyzeIntentUseCase
    private lateinit var pdfTextExtractor: PdfTextExtractor
    private lateinit var jobApplicationRepository: JobApplicationRepository
    private lateinit var exportManager: ExportManager
    private lateinit var gmailTokenManager: GmailTokenManager
    private lateinit var viewModel: ProfileViewModel

    private val fakeProfile = UserProfile(
        fullName = "Alice",
        careerGoal = "Senior Engineer",
        resumeText = "Experienced Kotlin developer"
    )

    private val fakeCareerProfile = CareerProfile(
        currentLevel = "Mid-level",
        targetRoles = listOf("Senior Android Engineer"),
        skillGaps = listOf("Leadership"),
        recommendedFocusAreas = listOf("Architecture"),
        goalMap = "AI-generated career goal summary"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userProfileDataStore = mockk(relaxed = true)
        analyzeIntentUseCase = mockk()
        pdfTextExtractor = mockk()
        jobApplicationRepository = mockk(relaxed = true)
        exportManager = mockk(relaxed = true)
        gmailTokenManager = mockk(relaxed = true)
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)
        coEvery { userProfileDataStore.gmailEmail } returns flowOf(null)
        coEvery { userProfileDataStore.userApiKey } returns flowOf(null)
        coEvery { userProfileDataStore.gmailNeedsReauth } returns flowOf(false)
        viewModel = ProfileViewModel(
            userProfileDataStore, analyzeIntentUseCase, pdfTextExtractor,
            jobApplicationRepository, exportManager, gmailTokenManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        assertTrue(viewModel.profileUiState.value is ProfileUiState.Idle)
    }

    @Test
    fun onResumePicked_withExtractedText_triggersAnalyzeIntent() = runTest {
        val mockUri = mockk<Uri>()
        val mockContext = mockk<Context>()
        coEvery { pdfTextExtractor.extract(any(), any()) } returns "extracted text"
        coEvery { analyzeIntentUseCase(any(), any()) } returns ClaudeResult.Success(fakeCareerProfile)

        viewModel.onResumePicked(mockUri, mockContext)
        advanceUntilIdle()

        coVerify(exactly = 1) { analyzeIntentUseCase(any(), any()) }
    }

    @Test
    fun onResumePicked_nullExtraction_emitsError() = runTest {
        val mockUri = mockk<Uri>()
        val mockContext = mockk<Context>()
        coEvery { pdfTextExtractor.extract(any(), any()) } returns null

        viewModel.onResumePicked(mockUri, mockContext)
        advanceUntilIdle()

        assertTrue(viewModel.profileUiState.value is ProfileUiState.Error)
    }

    @Test
    fun analyzeIntentIfNeeded_withEmptyResumeText_doesNotCallClaude() = runTest {
        val emptyProfile = fakeProfile.copy(resumeText = "")
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(emptyProfile)
        viewModel = ProfileViewModel(
            userProfileDataStore, analyzeIntentUseCase, pdfTextExtractor,
            jobApplicationRepository, exportManager, gmailTokenManager
        )

        viewModel.analyzeIntentIfNeeded()
        advanceUntilIdle()

        coVerify(exactly = 0) { analyzeIntentUseCase(any(), any()) }
    }

    @Test
    fun analyzeIntentIfNeeded_withResumeText_callsClaude() = runTest {
        coEvery { analyzeIntentUseCase(any(), any()) } returns ClaudeResult.Success(fakeCareerProfile)

        viewModel.analyzeIntentIfNeeded()
        advanceUntilIdle()

        coVerify(exactly = 1) { analyzeIntentUseCase(any(), any()) }
    }

    @Test
    fun analyzeIntentIfNeeded_onSuccess_updatesCareerGoalWithGoalMap() = runTest {
        coEvery { analyzeIntentUseCase(any(), any()) } returns ClaudeResult.Success(fakeCareerProfile)

        viewModel.analyzeIntentIfNeeded()
        advanceUntilIdle()

        coVerify { userProfileDataStore.update(any()) }
        assertTrue(viewModel.profileUiState.value is ProfileUiState.IntentAnalyzed)
    }

    @Test
    fun saveProfile_callsDataStoreUpdateWithCorrectFields() = runTest {
        viewModel.saveProfile(
            fullName = "Bob",
            careerGoal = "Lead Engineer",
            keywords = listOf("Android", "Kotlin"),
            targetSalaryMin = 100000,
            targetSalaryMax = 150000
        )
        advanceUntilIdle()

        coVerify {
            userProfileDataStore.update(match { block ->
                val result = block(fakeProfile)
                result.fullName == "Bob" &&
                        result.careerGoal == "Lead Engineer" &&
                        result.keywords == listOf("Android", "Kotlin") &&
                        result.targetSalaryMin == 100000 &&
                        result.targetSalaryMax == 150000
            })
        }
    }

    @Test
    fun saveProfile_transitesToSavedState() = runTest {
        viewModel.saveProfile("Bob", "Engineer", emptyList(), 0, 0)
        advanceUntilIdle()

        assertTrue(viewModel.profileUiState.value is ProfileUiState.Saved)
    }
}
