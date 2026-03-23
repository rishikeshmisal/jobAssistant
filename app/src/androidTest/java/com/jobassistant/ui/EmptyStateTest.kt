package com.jobassistant.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jobassistant.data.repository.UserProfileDataStore
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
class EmptyStateTest {

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
            // Delete all existing jobs so DB is empty
            val existing = jobApplicationRepository.getAllAsFlow().first()
            existing.forEach { jobApplicationRepository.delete(it) }
            // Skip onboarding so we land on Dashboard
            userProfileDataStore.update { copy(isOnboardingComplete = true) }
        }
    }

    @Test
    fun dashboard_emptyState_showsNoJobsMessage() {
        // Wait for Dashboard to be visible (not onboarding)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("No jobs yet").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No jobs yet").assertIsDisplayed()
    }

    @Test
    fun dashboard_emptyState_showsAddFirstJobButton() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Add your first job").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Add your first job").assertIsDisplayed()
    }
}
