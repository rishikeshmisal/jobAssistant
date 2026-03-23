package com.jobassistant.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jobassistant.data.repository.UserProfileDataStore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userProfileDataStore: UserProfileDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
        // Reset DataStore to initial state so each test starts at Onboarding
        runBlocking {
            userProfileDataStore.update {
                copy(isOnboardingComplete = false, fullName = "", careerGoal = "")
            }
        }
        // Wait for the UI to recompose to Onboarding
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Welcome to Job Assistant").fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun step1_nextButton_isDisabledWhenNameIsEmpty() {
        composeTestRule.onNodeWithText("Welcome to Job Assistant").assertIsDisplayed()
        composeTestRule.onNode(hasText("Next") and hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun step1_nextButton_isEnabledAfterNameEntry() {
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).assertIsEnabled()
    }

    @Test
    fun step2_nextButton_isDisabledWhenCareerGoalIsEmpty() {
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("What's your career goal?").assertIsDisplayed()
        composeTestRule.onNode(hasText("Next") and hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun step2_nextButton_isEnabledAfterCareerGoalEntry() {
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        composeTestRule.onNodeWithTag("career_goal_field").performTextInput("Land a senior role")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).assertIsEnabled()
    }

    @Test
    fun step3_resumeUpload_isVisible_withSkipOption() {
        // Navigate to step 1 (career goal)
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        // Navigate to step 2 (resume upload)
        composeTestRule.onNodeWithTag("career_goal_field").performTextInput("Land a senior role")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Upload Your Resume").assertIsDisplayed()
        composeTestRule.onNodeWithTag("skip_resume_button").assertIsDisplayed()
    }

    @Test
    fun completingAllSteps_navigatesToDashboard() {
        // Step 0: name
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        // Step 1: career goal
        composeTestRule.onNodeWithTag("career_goal_field").performTextInput("Land a senior role")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        // Step 2: resume upload — skip
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodesWithText("Upload Your Resume").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("skip_resume_button").performClick()

        // Step 3: connect Gmail
        composeTestRule.onNodeWithText("Connect Gmail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").performClick()

        // "Dashboard" appears in both screen placeholder and bottom nav
        composeTestRule.onAllNodesWithText("Dashboard").onFirst().assertIsDisplayed()
    }

    @Test
    fun step4_backButton_returnsToStep3() {
        // Navigate to step 3 (Gmail)
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        composeTestRule.onNodeWithTag("career_goal_field").performTextInput("Land a senior role")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodesWithText("Upload Your Resume").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("skip_resume_button").performClick()

        composeTestRule.onNodeWithText("Connect Gmail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").performClick()

        // Should return to step 2 (resume upload)
        composeTestRule.onNodeWithText("Upload Your Resume").assertIsDisplayed()
    }
}
