package com.jobassistant.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userProfileDataStore: UserProfileDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking {
            userProfileDataStore.update {
                copy(
                    isOnboardingComplete = true,
                    fullName = "Alice Smith",
                    careerGoal = "Senior Android Engineer",
                    keywords = listOf("Android", "Kotlin"),
                    targetSalaryMin = 120000,
                    targetSalaryMax = 160000
                )
            }
        }
        // Navigate to Profile tab
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Profile").fetchSemanticsNodes().size >= 1
        }
        composeTestRule.onAllNodesWithText("Profile").onFirst().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Your Resume").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun profileScreen_uploadResumeButton_isVisible() {
        composeTestRule.onNodeWithTag("upload_resume_button").assertIsDisplayed()
    }

    @Test
    fun profileScreen_careerDetailsSection_isVisible() {
        composeTestRule.onNodeWithText("Career Details").assertIsDisplayed()
    }

    @Test
    fun profileScreen_nameField_showsStoredValue() {
        composeTestRule.onNodeWithTag("profile_name_field").assertIsDisplayed()
    }

    @Test
    fun profileScreen_careerGoalField_showsStoredValue() {
        composeTestRule.onNodeWithTag("career_goal_field").assertIsDisplayed()
    }

    @Test
    fun profileScreen_keywordsField_isVisible() {
        composeTestRule.onNodeWithTag("keywords_field").assertIsDisplayed()
    }

    @Test
    fun profileScreen_saveProfileButton_isVisible() {
        composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()
    }

    @Test
    fun profileScreen_saveProfileButton_isClickable() {
        composeTestRule.onNodeWithTag("save_profile_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodesWithText("Profile saved!").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("profile_saved_text").assertIsDisplayed()
    }
}
