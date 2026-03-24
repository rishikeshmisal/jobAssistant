package com.jobassistant.ui.screens.dashboard

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.usecase.DeleteJobApplicationUseCase
import com.jobassistant.domain.usecase.GetAllJobsUseCase
import com.jobassistant.domain.usecase.UpdateJobStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getAllJobsUseCase: GetAllJobsUseCase
    private lateinit var updateJobStatusUseCase: UpdateJobStatusUseCase
    private lateinit var deleteJobApplicationUseCase: DeleteJobApplicationUseCase
    private lateinit var viewModel: DashboardViewModel

    private val savedJob = JobApplication(
        id = UUID.randomUUID(), companyName = "Acme", roleTitle = "Dev",
        status = ApplicationStatus.INTERESTED
    )
    private val appliedJob = JobApplication(
        id = UUID.randomUUID(), companyName = "Globex", roleTitle = "SWE",
        status = ApplicationStatus.APPLIED
    )
    private val interviewJob = JobApplication(
        id = UUID.randomUUID(), companyName = "Initech", roleTitle = "Lead",
        status = ApplicationStatus.INTERVIEWING
    )

    private val sampleJobs = listOf(savedJob, appliedJob, interviewJob)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllJobsUseCase = mockk()
        updateJobStatusUseCase = mockk(relaxed = true)
        deleteJobApplicationUseCase = mockk(relaxed = true)
        coEvery { getAllJobsUseCase() } returns flowOf(sampleJobs)
        viewModel = DashboardViewModel(getAllJobsUseCase, updateJobStatusUseCase, deleteJobApplicationUseCase)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `uiState jobsByStatus groups jobs correctly by ApplicationStatus`() = runTest {
        advanceUntilIdle()
        val grouped = viewModel.uiState.value.jobsByStatus

        assertEquals(listOf(savedJob), grouped[ApplicationStatus.INTERESTED])
        assertEquals(listOf(appliedJob), grouped[ApplicationStatus.APPLIED])
        assertEquals(listOf(interviewJob), grouped[ApplicationStatus.INTERVIEWING])
    }

    @Test
    fun `setStatus calls UpdateJobStatusUseCase with correct args`() = runTest {
        advanceUntilIdle()
        viewModel.setStatus(savedJob, ApplicationStatus.APPLIED)
        advanceUntilIdle()

        coVerify { updateJobStatusUseCase(savedJob.id, ApplicationStatus.APPLIED) }
    }

    @Test
    fun `deleteJob calls DeleteJobApplicationUseCase`() = runTest {
        advanceUntilIdle()
        viewModel.deleteJob(appliedJob)
        advanceUntilIdle()

        coVerify { deleteJobApplicationUseCase(appliedJob) }
    }

    @Test
    fun `setViewMode toggles viewMode correctly`() = runTest {
        advanceUntilIdle()
        assertEquals(ViewMode.KANBAN, viewModel.uiState.value.viewMode)

        viewModel.setViewMode(ViewMode.LIST)
        assertEquals(ViewMode.LIST, viewModel.uiState.value.viewMode)

        viewModel.setViewMode(ViewMode.KANBAN)
        assertEquals(ViewMode.KANBAN, viewModel.uiState.value.viewMode)
    }

    @Test
    fun `initial uiState has isLoading true until jobs are collected`() = runTest {
        // Re-create viewModel to capture initial state before coroutines run
        val newVm = DashboardViewModel(getAllJobsUseCase, updateJobStatusUseCase, deleteJobApplicationUseCase)
        // isLoading starts true
        assertEquals(true, newVm.uiState.value.isLoading)
        advanceUntilIdle()
        assertEquals(false, newVm.uiState.value.isLoading)
    }

    @Test
    fun `empty job list results in empty jobsByStatus map`() = runTest {
        coEvery { getAllJobsUseCase() } returns flowOf(emptyList())
        val newVm = DashboardViewModel(getAllJobsUseCase, updateJobStatusUseCase, deleteJobApplicationUseCase)
        advanceUntilIdle()

        assertEquals(emptyMap<ApplicationStatus, List<JobApplication>>(), newVm.uiState.value.jobsByStatus)
    }
}
