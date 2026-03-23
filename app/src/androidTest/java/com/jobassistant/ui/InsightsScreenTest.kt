package com.jobassistant.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InsightsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userProfileDataStore: UserProfileDataStore

    @Inject
    lateinit var jobApplicationRepository: JobApplicationRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking {
            // Clear existing jobs
            val existing = jobApplicationRepository.getAllAsFlow().first()
            existing.forEach { jobApplicationRepository.delete(it) }

            // Seed: 4 APPLIED, 2 INTERVIEWING, 3 REJECTED, 1 OFFERED
            // Total applied (non-SAVED) = 10
            // Interview rate = 2/10 = 20%
            // Rejection rate = 3/10 = 30%
            repeat(4) { i ->
                jobApplicationRepository.save(JobApplication(
                    companyName = "AppliedCo$i", roleTitle = "Dev$i",
                    status = ApplicationStatus.APPLIED
                ))
            }
            repeat(2) { i ->
                jobApplicationRepository.save(JobApplication(
                    companyName = "InterviewCo$i", roleTitle = "Dev$i",
                    status = ApplicationStatus.INTERVIEWING
                ))
            }
            repeat(3) { i ->
                jobApplicationRepository.save(JobApplication(
                    companyName = "RejectedCo$i", roleTitle = "Dev$i",
                    status = ApplicationStatus.REJECTED
                ))
            }
            jobApplicationRepository.save(JobApplication(
                companyName = "OfferCo", roleTitle = "Lead",
                status = ApplicationStatus.OFFERED
            ))

            // Skip onboarding
            userProfileDataStore.update { copy(isOnboardingComplete = true) }
        }
    }

    private fun navigateToInsights() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Insights").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Insights") and hasClickAction()).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Applied").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun insightsScreen_showsCorrectAppliedCount() {
        navigateToInsights()
        // Applied stat card should show 10 (all non-SAVED jobs)
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_showsCorrectInterviewCount() {
        navigateToInsights()
        // Interviews stat card = 2
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_showsCorrectRejectionCount() {
        navigateToInsights()
        // Rejections stat card = 3
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_showsCorrectOfferCount() {
        navigateToInsights()
        // Offers stat card = 1
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_showsAiCareerInsightsSection() {
        navigateToInsights()
        composeTestRule.onNodeWithText("AI Career Insights").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_showsRefreshButton() {
        navigateToInsights()
        composeTestRule.onNode(hasText("Refresh") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun insightsScreen_showsInterviewRateLabel() {
        navigateToInsights()
        // Rate bars section title
        composeTestRule.onNodeWithText("Conversion Rates").assertIsDisplayed()
    }

    @Test
    fun insightsScreen_emptyState_showsPromptMessage() {
        // Clear all jobs so we have no data
        runBlocking {
            val existing = jobApplicationRepository.getAllAsFlow().first()
            existing.forEach { jobApplicationRepository.delete(it) }
        }
        // Navigate to Insights
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Insights").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Insights") and hasClickAction()).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Apply to some jobs first to see insights")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Apply to some jobs first to see insights").assertIsDisplayed()
    }
}
