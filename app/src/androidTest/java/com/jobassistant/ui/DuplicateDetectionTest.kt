package com.jobassistant.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jobassistant.ui.screens.detail.DuplicateJobDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DuplicateDetectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun duplicateDialog_showsCompanyName() {
        composeTestRule.setContent {
            MaterialTheme {
                DuplicateJobDialog(
                    companyName = "Google",
                    roleTitle = "Android Engineer",
                    onDismiss = {},
                    onSaveAnyway = {}
                )
            }
        }
        // Company name appears in dialog body text
        composeTestRule.onNodeWithText("Google", substring = true).assertIsDisplayed()
    }

    @Test
    fun duplicateDialog_showsRoleTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                DuplicateJobDialog(
                    companyName = "Google",
                    roleTitle = "Android Engineer",
                    onDismiss = {},
                    onSaveAnyway = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Android Engineer", substring = true).assertIsDisplayed()
    }

    @Test
    fun duplicateDialog_cancelButton_callsOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            MaterialTheme {
                DuplicateJobDialog(
                    companyName = "Acme Corp",
                    roleTitle = "Engineer",
                    onDismiss = { dismissed = true },
                    onSaveAnyway = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("cancel_duplicate_button").performClick()
        assertTrue("onDismiss should be called", dismissed)
    }

    @Test
    fun duplicateDialog_saveAnywayButton_callsOnSaveAnyway() {
        var savedAnyway = false
        composeTestRule.setContent {
            MaterialTheme {
                DuplicateJobDialog(
                    companyName = "Acme Corp",
                    roleTitle = "Engineer",
                    onDismiss = {},
                    onSaveAnyway = { savedAnyway = true }
                )
            }
        }
        composeTestRule.onNodeWithTag("save_anyway_button").performClick()
        assertTrue("onSaveAnyway should be called", savedAnyway)
    }

    @Test
    fun duplicateDialog_showsTitleText() {
        composeTestRule.setContent {
            MaterialTheme {
                DuplicateJobDialog(
                    companyName = "Netflix",
                    roleTitle = "Backend Engineer",
                    onDismiss = {},
                    onSaveAnyway = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Duplicate Job").assertIsDisplayed()
    }
}
