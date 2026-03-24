package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class UpdateJobStatusUseCaseTest {

    private lateinit var repository: JobApplicationRepository
    private lateinit var useCase: UpdateJobStatusUseCase

    private val jobId = UUID.randomUUID()
    private val existingJob = JobApplication(
        id = jobId,
        companyName = "Meta",
        roleTitle = "Staff Engineer",
        status = ApplicationStatus.INTERESTED
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateJobStatusUseCase(repository)
    }

    @Test
    fun `updates status and calls save with updated job`() = runTest {
        coEvery { repository.getById(jobId) } returns existingJob
        coEvery { repository.save(any()) } returns Unit

        useCase(jobId, ApplicationStatus.APPLIED)

        coVerify {
            repository.save(withArg { saved ->
                assert(saved.status == ApplicationStatus.APPLIED)
                assert(saved.id == jobId)
                assert(saved.companyName == "Meta")
            })
        }
    }

    @Test
    fun `does nothing when job not found`() = runTest {
        coEvery { repository.getById(any()) } returns null

        useCase(UUID.randomUUID(), ApplicationStatus.INTERVIEWING)

        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `preserves all other fields when updating status`() = runTest {
        coEvery { repository.getById(jobId) } returns existingJob
        coEvery { repository.save(any()) } returns Unit

        useCase(jobId, ApplicationStatus.OFFER)

        coVerify {
            repository.save(withArg { saved ->
                assert(saved.status == ApplicationStatus.OFFER)
                assert(saved.companyName == existingJob.companyName)
                assert(saved.roleTitle == existingJob.roleTitle)
                assert(saved.notes == existingJob.notes)
            })
        }
    }
}
