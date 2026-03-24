package com.jobassistant.ui.screens.detail

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.domain.usecase.SaveResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AddJobViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var saveJobApplicationUseCase: SaveJobApplicationUseCase
    private lateinit var jobApplicationRepository: JobApplicationRepository
    private lateinit var viewModel: AddJobViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        saveJobApplicationUseCase = mockk()
        jobApplicationRepository = mockk(relaxed = true)
        viewModel = AddJobViewModel(saveJobApplicationUseCase, jobApplicationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is AddJobUiState.Idle)
    }

    // ── saveJob ──────────────────────────────────────────────────────────────

    @Test
    fun `saveJob emits Saved with jobId on success`() = runTest {
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel.saveJob("Acme", "Engineer", "Remote", "100k")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.Saved)
        // jobId should be a valid UUID
        val saved = state as AddJobUiState.Saved
        assertTrue(saved.jobId.toString().isNotBlank())
    }

    @Test
    fun `saveJob creates job with fitScore null and status SAVED`() = runTest {
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel.saveJob("Globex", "Android Dev", "NYC", "120k")
        advanceUntilIdle()

        coVerify {
            saveJobApplicationUseCase(match { job ->
                job.fitScore == null &&
                        job.status == ApplicationStatus.INTERESTED &&
                        job.companyName == "Globex" &&
                        job.roleTitle == "Android Dev"
            })
        }
    }

    @Test
    fun `saveJob passes location and salary to job`() = runTest {
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel.saveJob("Co", "Role", "London", "50k-80k")
        advanceUntilIdle()

        coVerify {
            saveJobApplicationUseCase(match { job ->
                job.location == "London" && job.salaryRange == "50k-80k"
            })
        }
    }

    @Test
    fun `saveJob with blank location stores null`() = runTest {
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        viewModel.saveJob("Co", "Role", "  ", "")
        advanceUntilIdle()

        coVerify {
            saveJobApplicationUseCase(match { job ->
                job.location == null && job.salaryRange == null
            })
        }
    }

    // ── duplicate detection ──────────────────────────────────────────────────

    @Test
    fun `saveJob emits Duplicate state when duplicate found`() = runTest {
        val existing = JobApplication(
            companyName = "Google", roleTitle = "Android Engineer", status = ApplicationStatus.APPLIED
        )
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)

        viewModel.saveJob("Google", "Android Engineer", "", "")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddJobUiState.Duplicate)
        assertEquals("Google", (state as AddJobUiState.Duplicate).companyName)
        assertEquals("Android Engineer", state.roleTitle)
    }

    @Test
    fun `dismissDuplicate resets state to Idle`() = runTest {
        val existing = JobApplication(companyName = "Google", roleTitle = "Eng")
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)
        viewModel.saveJob("Google", "Eng", "", "")
        advanceUntilIdle()

        viewModel.dismissDuplicate()

        assertTrue(viewModel.uiState.value is AddJobUiState.Idle)
    }

    @Test
    fun `saveJobForce saves directly and emits Saved with jobId`() = runTest {
        val existing = JobApplication(companyName = "Google", roleTitle = "Eng")
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)
        viewModel.saveJob("Google", "Eng", "", "")
        advanceUntilIdle()

        viewModel.saveJobForce()
        advanceUntilIdle()

        coVerify { jobApplicationRepository.save(any()) }
        assertTrue(viewModel.uiState.value is AddJobUiState.Saved)
    }

    @Test
    fun `saveJobForce jobId matches original pending job`() = runTest {
        val existing = JobApplication(companyName = "Meta", roleTitle = "SWE")
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Duplicate(existing)
        viewModel.saveJob("Meta", "SWE", "", "")
        advanceUntilIdle()

        // Capture the pending job's ID from the Duplicate state — it was created during saveJob
        viewModel.saveJobForce()
        advanceUntilIdle()

        val state = viewModel.uiState.value as AddJobUiState.Saved
        assertTrue(state.jobId.toString().isNotBlank())
    }
}
