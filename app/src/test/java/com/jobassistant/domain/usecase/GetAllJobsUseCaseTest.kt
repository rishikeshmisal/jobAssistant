package com.jobassistant.domain.usecase

import app.cash.turbine.test
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

class GetAllJobsUseCaseTest {

    private lateinit var repository: JobApplicationRepository
    private lateinit var useCase: GetAllJobsUseCase

    private val job1 = JobApplication(id = UUID.randomUUID(), companyName = "Apple", roleTitle = "iOS Dev")
    private val job2 = JobApplication(id = UUID.randomUUID(), companyName = "Netflix", roleTitle = "Backend Dev")

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllJobsUseCase(repository)
    }

    @Test
    fun `delegates to repository getAllAsFlow`() = runTest {
        every { repository.getAllAsFlow() } returns flowOf(listOf(job1, job2))

        useCase().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Apple", result[0].companyName)
            assertEquals("Netflix", result[1].companyName)
            awaitComplete()
        }

        verify { repository.getAllAsFlow() }
    }

    @Test
    fun `emits empty list when no jobs`() = runTest {
        every { repository.getAllAsFlow() } returns flowOf(emptyList())

        useCase().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `emits multiple updates from flow`() = runTest {
        every { repository.getAllAsFlow() } returns kotlinx.coroutines.flow.flow {
            emit(listOf(job1))
            emit(listOf(job1, job2))
        }

        useCase().test {
            assertEquals(1, awaitItem().size)
            assertEquals(2, awaitItem().size)
            awaitComplete()
        }
    }
}
