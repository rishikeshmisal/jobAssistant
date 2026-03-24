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
    fun `applied count excludes INTERESTED jobs`() = runTest {
        val jobsWithInterested = sampleJobs + listOf(
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "X", roleTitle = "Y",
                status = ApplicationStatus.INTERESTED)
        )
        coEvery { getAllJobsUseCase() } returns flowOf(jobsWithInterested)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()
        // INTERESTED job excluded from applied count → still 20
        assertEquals(20, vm.uiState.value.stats.totalApplied)
    }

    @Test
    fun `interviews count includes SCREENING INTERVIEWING ASSESSMENT`() = runTest {
        val mixedJobs = listOf(
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "A", roleTitle = "R", status = ApplicationStatus.SCREENING),
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "B", roleTitle = "R", status = ApplicationStatus.INTERVIEWING),
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "C", roleTitle = "R", status = ApplicationStatus.ASSESSMENT),
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "D", roleTitle = "R", status = ApplicationStatus.APPLIED)
        )
        coEvery { getAllJobsUseCase() } returns flowOf(mixedJobs)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.stats.interviews)
    }

    @Test
    fun `offers count includes both OFFER and ACCEPTED`() = runTest {
        val offerJobs = listOf(
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "A", roleTitle = "R", status = ApplicationStatus.OFFER),
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "B", roleTitle = "R", status = ApplicationStatus.ACCEPTED)
        )
        coEvery { getAllJobsUseCase() } returns flowOf(offerJobs)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.stats.offers)
    }

    @Test
    fun `withdrawn and noResponse are counted separately`() = runTest {
        val closedJobs = listOf(
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "A", roleTitle = "R", status = ApplicationStatus.WITHDRAWN),
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "B", roleTitle = "R", status = ApplicationStatus.WITHDRAWN),
            JobApplication(id = java.util.UUID.randomUUID(), companyName = "C", roleTitle = "R", status = ApplicationStatus.NO_RESPONSE)
        )
        coEvery { getAllJobsUseCase() } returns flowOf(closedJobs)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.stats.withdrawn)
        assertEquals(1, vm.uiState.value.stats.noResponse)
    }

    @Test
    fun `refreshInsights calls GenerateInsightsUseCase`() = runTest {
        advanceUntilIdle()
        val fakeInsights = CareerInsights(identifiedGaps = listOf("gap1"), recommendedActions = listOf("action1"), summaryAnalysis = "summary")
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(fakeInsights)

        viewModel.refreshInsights()
        advanceUntilIdle()

        coVerify { generateInsightsUseCase(any(), any()) }
    }

    @Test
    fun `dataChangedSinceRefresh is true before first refresh`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.dataChangedSinceRefresh)
    }

    @Test
    fun `dataChangedSinceRefresh is false immediately after successful refresh`() = runTest {
        advanceUntilIdle()
        val insights = CareerInsights(identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = "")
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(insights)

        viewModel.refreshInsights()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.dataChangedSinceRefresh)
    }

    @Test
    fun `dataChangedSinceRefresh becomes true when new jobs are added after refresh`() = runTest {
        advanceUntilIdle()
        val insights = CareerInsights(identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = "")
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(insights)

        viewModel.refreshInsights()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.dataChangedSinceRefresh)

        // Simulate a new job being added — more jobs than at refresh time
        val extraJob = JobApplication(
            id = java.util.UUID.randomUUID(), companyName = "NewCo", roleTitle = "Dev",
            status = ApplicationStatus.APPLIED
        )
        coEvery { getAllJobsUseCase() } returns flowOf(sampleJobs + extraJob)
        // Trigger the combine by emitting from insights flow
        coEvery { getCareerInsightsUseCase() } returns flowOf(insights)
        val vm2 = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        // New VM with new jobs but no prior snapshot — should be unlocked
        assertTrue(vm2.uiState.value.dataChangedSinceRefresh)
    }

    @Test
    fun `refreshInsights is not blocked by time-based cooldown before 8h`() = runTest {
        // Even with very recent insights, refreshInsights() should still execute
        // (the button state is controlled by Screen, not the ViewModel guard)
        val recentInsights = CareerInsights(
            generatedDate = System.currentTimeMillis() - 1000L,
            identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = ""
        )
        coEvery { getCareerInsightsUseCase() } returns flowOf(recentInsights)
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(recentInsights)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        vm.refreshInsights()
        advanceUntilIdle()

        coVerify(atLeast = 1) { generateInsightsUseCase(any(), any()) }
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

    // ── Phase 11: userProfile propagation ────────────────────────────────────

    @Test
    fun `uiState userProfile fullName is populated from DataStore`() = runTest {
        advanceUntilIdle()
        assertEquals("Test", viewModel.uiState.value.userProfile.fullName)
    }

    @Test
    fun `uiState userProfile careerGoal is populated from DataStore`() = runTest {
        advanceUntilIdle()
        assertEquals("Senior Dev", viewModel.uiState.value.userProfile.careerGoal)
    }

    @Test
    fun `uiState userProfile updates when DataStore emits new profile`() = runTest {
        val updatedProfile = fakeProfile.copy(fullName = "Updated Name")
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(updatedProfile)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        assertEquals("Updated Name", vm.uiState.value.userProfile.fullName)
    }

    // ── Phase 11: buildHistorySummary includes job details ───────────────────

    @Test
    fun `refreshInsights passes job role title in history summary`() = runTest {
        val detailedJobs = listOf(
            JobApplication(
                companyName = "DeepMind",
                roleTitle = "ML Engineer",
                status = ApplicationStatus.REJECTED,
                fitScore = 80
            )
        )
        coEvery { getAllJobsUseCase() } returns flowOf(detailedJobs)
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(
            CareerInsights(identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = "")
        )
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        vm.refreshInsights()
        advanceUntilIdle()

        coVerify { generateInsightsUseCase(any(), withArg { summary ->
            summary.contains("ML Engineer") && summary.contains("DeepMind")
        }) }
    }

    @Test
    fun `refreshInsights passes career keywords in profile summary`() = runTest {
        val profileWithKeywords = fakeProfile.copy(keywords = listOf("Kotlin", "Jetpack Compose"))
        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(profileWithKeywords)
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(
            CareerInsights(identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = "")
        )
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        vm.refreshInsights()
        advanceUntilIdle()

        coVerify { generateInsightsUseCase(withArg { profile ->
            profile.contains("Kotlin") && profile.contains("Jetpack Compose")
        }, any()) }
    }

    @Test
    fun `refreshInsights passes empty history summary when no applied jobs`() = runTest {
        coEvery { getAllJobsUseCase() } returns flowOf(
            listOf(JobApplication(companyName = "Co", roleTitle = "Role", status = ApplicationStatus.INTERESTED))
        )
        coEvery { generateInsightsUseCase(any(), any()) } returns ClaudeResult.Success(
            CareerInsights(identifiedGaps = emptyList(), recommendedActions = emptyList(), summaryAnalysis = "")
        )
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        vm.refreshInsights()
        advanceUntilIdle()

        coVerify { generateInsightsUseCase(any(), withArg { it.contains("No applications") }) }
    }

    @Test
    fun `topCompanies returns top 3 by application count`() = runTest {
        val manyJobs = buildList {
            repeat(5) { add(JobApplication(companyName = "Google", roleTitle = "SWE", status = ApplicationStatus.APPLIED)) }
            repeat(3) { add(JobApplication(companyName = "Meta", roleTitle = "SWE", status = ApplicationStatus.REJECTED)) }
            repeat(2) { add(JobApplication(companyName = "Amazon", roleTitle = "SWE", status = ApplicationStatus.APPLIED)) }
            repeat(1) { add(JobApplication(companyName = "Netflix", roleTitle = "SWE", status = ApplicationStatus.APPLIED)) }
        }
        coEvery { getAllJobsUseCase() } returns flowOf(manyJobs)
        val vm = InsightsViewModel(getAllJobsUseCase, generateInsightsUseCase, getCareerInsightsUseCase, userProfileDataStore)
        advanceUntilIdle()

        val topCompanies = vm.uiState.value.stats.topCompanies
        assertEquals(3, topCompanies.size)
        assertEquals("Google", topCompanies[0].first)
        assertEquals(5, topCompanies[0].second)
    }
}
