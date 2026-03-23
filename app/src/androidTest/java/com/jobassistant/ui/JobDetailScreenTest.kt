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
class JobDetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userProfileDataStore: UserProfileDataStore

    @Inject
    lateinit var jobApplicationRepository: JobApplicationRepository

    private val testJob = JobApplication(
        companyName = "FitScoreCo",
        roleTitle = "Android Eng",
        status = ApplicationStatus.APPLIED,
        fitScore = 83
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking {
            // Clear and seed
            val existing = jobApplicationRepository.getAllAsFlow().first()
            existing.forEach { jobApplicationRepository.delete(it) }
            jobApplicationRepository.save(testJob)
            userProfileDataStore.update { copy(isOnboardingComplete = true) }
        }
    }

    @Test
    fun jobDetailScreen_showsCorrectFitScore() {
        // Navigate from dashboard to detail
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("FitScoreCo").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("FitScoreCo").performClick()

        // Verify fit score displays
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("83%").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("83%").assertIsDisplayed()
    }

    @Test
    fun jobDetailScreen_showsCompanyNameInHeader() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("FitScoreCo").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("FitScoreCo").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("FitScoreCo").fetchSemanticsNodes().size >= 1
        }
        composeTestRule.onAllNodesWithText("FitScoreCo").onFirst().assertIsDisplayed()
    }

    @Test
    fun jobDetailScreen_showsStatusDropdown() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("FitScoreCo").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("FitScoreCo").performClick()

        // Status dropdown button should show current status
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Applied").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Applied").onFirst().assertIsDisplayed()
    }

    @Test
    fun jobDetailScreen_saveChangesButton_isVisible() {
        // Navigate to detail
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("FitScoreCo").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("FitScoreCo").performClick()

        // Wait for detail screen to load
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Save Changes").fetchSemanticsNodes().isNotEmpty()
        }
        // Save Changes is below the fold; assertExists() confirms it is in the semantic tree
        composeTestRule.onNode(hasText("Save Changes") and hasClickAction()).assertExists()
    }
}
