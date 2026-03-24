package com.jobassistant.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jobassistant.domain.model.AppTheme
import com.jobassistant.ui.components.ThemeSelector
import com.jobassistant.ui.theme.JobAssistantTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allFourColorChips_areDisplayed() {
        composeTestRule.setContent {
            JobAssistantTheme {
                ThemeSelector(
                    selectedTheme = AppTheme.GREEN,
                    onThemeSelected = {}
                )
            }
        }

        // Each chip shows first letter of theme name
        composeTestRule.onNodeWithText("G").assertExists()
        composeTestRule.onNodeWithText("R").assertExists()
        composeTestRule.onNodeWithText("B").assertExists()
        composeTestRule.onNodeWithText("Y").assertExists()
    }

    @Test
    fun tappingGreenChip_firesCallbackWithGreen() {
        var selected: AppTheme? = null
        composeTestRule.setContent {
            JobAssistantTheme {
                ThemeSelector(
                    selectedTheme = AppTheme.BLUE,
                    onThemeSelected = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("G").performClick()
        assertEquals(AppTheme.GREEN, selected)
    }

    @Test
    fun tappingRedChip_firesCallbackWithRed() {
        var selected: AppTheme? = null
        composeTestRule.setContent {
            JobAssistantTheme {
                ThemeSelector(
                    selectedTheme = AppTheme.GREEN,
                    onThemeSelected = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("R").performClick()
        assertEquals(AppTheme.RED, selected)
    }

    @Test
    fun tappingBlueChip_firesCallbackWithBlue() {
        var selected: AppTheme? = null
        composeTestRule.setContent {
            JobAssistantTheme {
                ThemeSelector(
                    selectedTheme = AppTheme.GREEN,
                    onThemeSelected = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("B").performClick()
        assertEquals(AppTheme.BLUE, selected)
    }

    @Test
    fun tappingYellowChip_firesCallbackWithYellow() {
        var selected: AppTheme? = null
        composeTestRule.setContent {
            JobAssistantTheme {
                ThemeSelector(
                    selectedTheme = AppTheme.GREEN,
                    onThemeSelected = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("P").performClick()
        assertEquals(AppTheme.PURPLE, selected)
    }
}
