package com.jobassistant.ui

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.EvaluateFitUseCase
import com.jobassistant.domain.usecase.FetchUrlUseCase
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.ui.screens.detail.AddJobScreen
import com.jobassistant.ui.screens.detail.AddJobViewModel
import com.jobassistant.util.OcrProcessor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 8 - AddJobScreenOcrTabTest (Compose UI test)
 *
 * Verifies Screenshot tab visibility, Pick Screenshot button presence, OCR text preview,
 * and Analyze Fit button enablement — per Phase 8 Testing Requirements.
 */
@RunWith(AndroidJUnit4::class)
class AddJobScreenOcrTabTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var ocrProcessor: OcrProcessor
    private lateinit var evaluateFitUseCase: EvaluateFitUseCase
    private lateinit var saveJobUseCase: SaveJobApplicationUseCase
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var fetchUrlUseCase: FetchUrlUseCase
    private lateinit var jobRepository: JobApplicationRepository
    private lateinit var viewModel: AddJobViewModel

    private val fakeProfile = UserProfile(
        userId = "u1",
        fullName = "Test User",
        resumeText = "Android developer",
        keywords = emptyList(),
        careerGoal = "Senior Engineer",
        targetSalaryMin = 0,
        targetSalaryMax = 0
    )

    @Before
    fun setUp() {
        ocrProcessor = mockk()
        evaluateFitUseCase = mockk()
        saveJobUseCase = mockk(relaxed = true)
        userProfileDataStore = mockk()
        fetchUrlUseCase = mockk()
        jobRepository = mockk(relaxed = true)

        coEvery { userProfileDataStore.userProfileFlow } returns flowOf(fakeProfile)

        viewModel = AddJobViewModel(
            evaluateFitUseCase,
            saveJobUseCase,
            userProfileDataStore,
            fetchUrlUseCase,
            jobRepository,
            ocrProcessor
        )
    }

    @Test
    fun screenshotTab_pickScreenshotButton_isVisible_afterTappingTab() {
        composeRule.setContent {
            AddJobScreen(viewModel = viewModel)
        }

        // Tap the Screenshot tab chip
        composeRule.onNodeWithTag("mode_screenshot_chip").performClick()

        // "Pick Screenshot" button should be visible
        composeRule.onNodeWithTag("pick_screenshot_button").assertIsDisplayed()
    }

    @Test
    fun screenshotTab_analyzeButton_disabledWhenNoOcrText() {
        composeRule.setContent {
            AddJobScreen(viewModel = viewModel)
        }

        // Switch to Screenshot tab
        composeRule.onNodeWithTag("mode_screenshot_chip").performClick()

        // Analyze Fit button must be disabled (no OCR text yet)
        composeRule.onNodeWithTag("analyze_fit_button").assertIsNotEnabled()
    }

    @Test
    fun screenshotTab_ocrTextPreview_showsExtractedTextAndEnablesAnalyze() {
        val extractedText = "Principal Android Engineer – Kotlin, Coroutines, Hilt"

        composeRule.setContent {
            AddJobScreen(viewModel = viewModel)
        }

        // Switch to Screenshot tab
        composeRule.onNodeWithTag("mode_screenshot_chip").performClick()

        // Inject OCR text directly via test helper (bypasses real ML Kit call)
        composeRule.runOnUiThread { viewModel.setOcrTextForTesting(extractedText) }

        // OCR text preview should be visible and contain the extracted text
        composeRule.onNodeWithTag("ocr_text_preview").assertIsDisplayed()
        composeRule.onNodeWithText(extractedText, substring = true).assertIsDisplayed()

        // Analyze Fit button should now be enabled
        composeRule.onNodeWithTag("analyze_fit_button").assertIsEnabled()
    }

    @Test
    fun screenshotTab_analyzeButton_emitsFitResult_afterOcrText() {
        val extractedText = "Senior ML Engineer at DeepMind – PyTorch, distributed training"
        val fitAnalysis = FitAnalysis(
            score = 78,
            pros = listOf("ML background"),
            cons = listOf("No PyTorch experience"),
            missingSkills = listOf("PyTorch")
        )
        coEvery { evaluateFitUseCase(any(), any()) } returns ClaudeResult.Success(fitAnalysis)

        composeRule.setContent {
            AddJobScreen(viewModel = viewModel)
        }

        composeRule.onNodeWithTag("mode_screenshot_chip").performClick()
        composeRule.runOnUiThread { viewModel.setOcrTextForTesting(extractedText) }

        // Tap Analyze Fit
        composeRule.onNodeWithTag("analyze_fit_button").performClick()

        // Wait for coroutines
        composeRule.waitForIdle()

        // Fit score should be visible
        composeRule.onNodeWithTag("fit_score").assertIsDisplayed()
        composeRule.onNodeWithText("78", substring = true).assertIsDisplayed()
    }

    @Test
    fun screenshotTab_isOpenedDirectlyWhenInitialImageUriProvided() {
        // Provide a non-null initialImageUri so the screen auto-switches to Screenshot tab
        val testUri = Uri.parse("content://test/image.png")
        coEvery { ocrProcessor.extractText(any()) } throws RuntimeException("No real bitmap in test")

        composeRule.setContent {
            // Pass initialImageUri — the LaunchedEffect switches to Screenshot tab
            // We don't await OCR here; just verify the tab switch happens
            AddJobScreen(
                viewModel = viewModel,
                initialImageUri = testUri
            )
        }

        // Screenshot tab chip should be selected (screen auto-switches)
        composeRule.onNodeWithTag("mode_screenshot_chip").assertIsDisplayed()
        composeRule.onNodeWithTag("pick_screenshot_button").assertIsDisplayed()
    }
}
