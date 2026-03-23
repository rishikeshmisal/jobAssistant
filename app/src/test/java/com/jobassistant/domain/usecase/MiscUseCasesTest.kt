package com.jobassistant.domain.usecase

import app.cash.turbine.test
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.CareerInsightsRepository
import com.jobassistant.domain.repository.JobApplicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MiscUseCasesTest {

    private lateinit var jobRepo: JobApplicationRepository
    private lateinit var insightsRepo: CareerInsightsRepository

    private val job = JobApplication(
        id = UUID.randomUUID(),
        companyName = "Amazon",
        roleTitle = "SDE"
    )
    private val insights = CareerInsights(
        id = UUID.randomUUID(),
        generatedDate = 1000L,
        summaryAnalysis = "Some analysis"
    )

    @Before
    fun setUp() {
        jobRepo = mockk()
        insightsRepo = mockk()
    }

    // DeleteJobApplicationUseCase
    @Test
    fun `DeleteJobApplicationUseCase calls delete on repository`() = runTest {
        coEvery { jobRepo.delete(job) } returns Unit
        val useCase = DeleteJobApplicationUseCase(jobRepo)

        useCase(job)

        coVerify { jobRepo.delete(job) }
    }

    // GetJobsByStatusUseCase
    @Test
    fun `GetJobsByStatusUseCase delegates to getByStatusAsFlow`() = runTest {
        every { jobRepo.getByStatusAsFlow(ApplicationStatus.APPLIED) } returns flowOf(listOf(job))
        val useCase = GetJobsByStatusUseCase(jobRepo)

        useCase(ApplicationStatus.APPLIED).test {
            assertEquals(1, awaitItem().size)
            awaitComplete()
        }

        verify { jobRepo.getByStatusAsFlow(ApplicationStatus.APPLIED) }
    }

    @Test
    fun `GetJobsByStatusUseCase filters by status correctly`() = runTest {
        val savedJob = job.copy(status = ApplicationStatus.SAVED)
        every { jobRepo.getByStatusAsFlow(ApplicationStatus.SAVED) } returns flowOf(listOf(savedJob))
        val useCase = GetJobsByStatusUseCase(jobRepo)

        useCase(ApplicationStatus.SAVED).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(ApplicationStatus.SAVED, result[0].status)
            awaitComplete()
        }
    }

    // GetCareerInsightsUseCase
    @Test
    fun `GetCareerInsightsUseCase delegates to getLatestAsFlow`() = runTest {
        every { insightsRepo.getLatestAsFlow() } returns flowOf(insights)
        val useCase = GetCareerInsightsUseCase(insightsRepo)

        useCase().test {
            val result = awaitItem()
            assertEquals("Some analysis", result?.summaryAnalysis)
            awaitComplete()
        }

        verify { insightsRepo.getLatestAsFlow() }
    }

    @Test
    fun `GetCareerInsightsUseCase emits null when no insights`() = runTest {
        every { insightsRepo.getLatestAsFlow() } returns flowOf(null)
        val useCase = GetCareerInsightsUseCase(insightsRepo)

        useCase().test {
            assertEquals(null, awaitItem())
            awaitComplete()
        }
    }

    // SaveCareerInsightsUseCase
    @Test
    fun `SaveCareerInsightsUseCase calls save on repository`() = runTest {
        coEvery { insightsRepo.save(insights) } returns Unit
        val useCase = SaveCareerInsightsUseCase(insightsRepo)

        useCase(insights)

        coVerify { insightsRepo.save(insights) }
    }

    @Test
    fun `SaveCareerInsightsUseCase passes insights unchanged`() = runTest {
        coEvery { insightsRepo.save(any()) } returns Unit
        val useCase = SaveCareerInsightsUseCase(insightsRepo)

        useCase(insights)

        coVerify { insightsRepo.save(withArg { assertEquals("Some analysis", it.summaryAnalysis) }) }
    }

    // JobApplicationRepositoryImpl getByStatusAsFlow coverage
    @Test
    fun `GetJobsByStatusUseCase emits empty list for status with no matches`() = runTest {
        every { jobRepo.getByStatusAsFlow(ApplicationStatus.REJECTED) } returns flowOf(emptyList())
        val useCase = GetJobsByStatusUseCase(jobRepo)

        useCase(ApplicationStatus.REJECTED).test {
            assertEquals(0, awaitItem().size)
            awaitComplete()
        }
    }
}
