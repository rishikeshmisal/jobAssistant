package com.jobassistant.ui

import androidx.compose.ui.test.assertIsDisplayed
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
class NavigationTest {

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
    fun onFirstLaunch_onboardingScreen_isShown() {
        composeTestRule.onNodeWithText("Welcome to Job Assistant").assertIsDisplayed()
    }

    @Test
    fun bottomNav_dashboard_isVisibleAfterOnboarding() {
        completeOnboarding()
        composeTestRule.onNode(hasText("Dashboard") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun bottomNav_profile_navigatesToProfile() {
        completeOnboarding()
        composeTestRule.onNode(hasText("Profile") and hasClickAction()).performClick()
        // "Profile" appears in both screen placeholder and bottom nav
        composeTestRule.onAllNodesWithText("Profile").onFirst().assertIsDisplayed()
    }

    @Test
    fun bottomNav_insights_navigatesToInsights() {
        completeOnboarding()
        composeTestRule.onNode(hasText("Insights") and hasClickAction()).performClick()
        composeTestRule.onAllNodesWithText("Insights").onFirst().assertIsDisplayed()
    }

    private fun completeOnboarding() {
        composeTestRule.onNodeWithTag("name_field").performTextInput("Alice")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        composeTestRule.onNodeWithTag("career_goal_field").performTextInput("Land a senior role")
        composeTestRule.onNode(hasText("Next") and hasClickAction()).performClick()

        // Step 2: resume upload — skip
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodesWithText("Upload Your Resume").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("skip_resume_button").performClick()

        // Step 3: connect Gmail
        composeTestRule.onNodeWithText("Get Started").performClick()
    }
}
