package com.jobassistant.ui.screens.insights

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.usecase.GenerateInsightsUseCase
import com.jobassistant.domain.usecase.GetAllJobsUseCase
import com.jobassistant.domain.usecase.GetCareerInsightsUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getAllJobsUseCase: GetAllJobsUseCase
    private lateinit var generateInsightsUseCase: GenerateInsightsUseCase
    private lateinit var getCareerInsightsUseCase: GetCareerInsightsUseCase
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var viewModel: InsightsViewModel

    private val fakeProfile = UserProfile(
        userId = "u1", fullName = "Test", resumeText = "resume", keywords = emptyList(),
        careerGoal = "Senior Dev", targetSalaryMin = 0, targetSalaryMax = 0
    )

    // 10 APPLIED, 3 INTERVIEWING, 7 REJECTED → applied total = 20, interviews = 3, rejections = 7
    private val sampleJobs: List<JobApplication> = buildList {
        repeat(10) {
            add(JobApplication(id = UUID.randomUUID(), companyName = "Co$it", roleTitle = "R$it",
                status = ApplicationStatus.APPLIED))
        }
        repeat(3) {
            add(JobApplication(id = UUID.randomUUID(), companyName = "Int$it", roleTitle = "R$it",
                status = ApplicationStatus.INTERVIEWING))
        }
        repeat(7) {
            add(JobApplication(id = UUID.randomUUID(), companyName = "Rej$it", roleTitle = "R$it",
                status = ApplicationStatus.REJECTED))
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllJobsUseCase = mockk()
        generateInsightsUseCase = mockk(relaxed = true)
        getCareerInsightsUseCase = mockk()
        userProfileDataStore = mockk()

        coEvery { getAllJobsUseCase() } returns flowOf(sampleJobs)
        coEvery { getCareerInsightsUseCase() } returns flowOf(null)
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)

        viewModel = InsightsViewModel(
            getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore
        )
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `interviewRate is 15 percent for 3 interviews out of 20 applied`() = runTest {
        advanceUntilIdle()
        // applied = APPLIED(10) + INTERVIEWING(3) + REJECTED(7) = 20
        // interviews = 3
        // rate = 3/20 * 100 = 15%
        assertEquals(15f, viewModel.uiState.value.stats.interviewRate, 0.01f)
    }

    @Test
    fun `rejectionRate is 35 percent for 7 rejections out of 20 applied`() = runTest {
        advanceUntilIdle()
        // rejections = 7, applied = 20 → 35%
        assertEquals(35f, viewModel.uiState.value.stats.rejectionRate, 0.01f)
    }

    @Test
    fun `aggregate stats counts are correct`() = runTest {
        advanceUntilIdle()
        val stats = viewModel.uiState.value.stats
        assertEquals(20, stats.totalApplied)
        assertEquals(3, stats.interviews)
        assertEquals(7, stats.rejections)
        assertEquals(0, stats.offers)
    }

    @Test
    fun `refreshInsights calls GenerateInsightsUseCase when isRefreshEnabled`() = runTest {
        advanceUntilIdle()
        val fakeInsights = CareerInsights(identifiedGaps = listOf("gap1"), recommendedActions = listOf("action1"), summaryAnalysis = "summary")
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(fakeInsights)
        assertTrue(viewModel.uiState.value.isRefreshEnabled)

        viewModel.refreshInsights()
        advanceUntilIdle()

        coVerify { generateInsightsUseCase(any(), any()) }
    }

    @Test
    fun `refreshInsights does NOT call GenerateInsightsUseCase when isRefreshEnabled is false`() = runTest {
        // Seed cached insights with recent timestamp (within 24h)
        val recentInsights = CareerInsights(
            generatedDate = System.currentTimeMillis() - 1000L, // 1 second ago
            identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = ""
        )
        coEvery { getCareerInsightsUseCase() } returns flowOf(recentInsights)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isRefreshEnabled)

        vm.refreshInsights()
        advanceUntilIdle()

        coVerify(exactly = 0) { generateInsightsUseCase(any(), any()) }
    }

    @Test
    fun `refreshInsights updates insights state on success`() = runTest {
        advanceUntilIdle()
        val newInsights = CareerInsights(
            identifiedGaps = listOf("Python"), recommendedActions = listOf("Learn Python"), summaryAnalysis = "Good overall"
        )
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(newInsights)

        viewModel.refreshInsights()
        advanceUntilIdle()

        assertEquals(newInsights, viewModel.uiState.value.insights)
    }

    @Test
    fun `empty job list results in zero stats`() = runTest {
        coEvery { getAllJobsUseCase() } returns flowOf(emptyList())
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        val stats = vm.uiState.value.stats
        assertEquals(0, stats.totalApplied)
        assertEquals(0f, stats.interviewRate, 0.01f)
        assertEquals(0f, stats.rejectionRate, 0.01f)
    }
}
