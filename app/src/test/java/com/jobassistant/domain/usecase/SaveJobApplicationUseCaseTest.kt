package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class SaveJobApplicationUseCaseTest {

    private lateinit var repository: JobApplicationRepository
    private lateinit var useCase: SaveJobApplicationUseCase

    private val job = JobApplication(
        id = UUID.randomUUID(),
        companyName = "Google",
        roleTitle = "Android Engineer"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SaveJobApplicationUseCase(repository)
    }

    @Test
    fun `returns Saved when no duplicate found`() = runTest {
        coEvery { repository.findDuplicate(any(), any()) } returns null
        coEvery { repository.save(any()) } returns Unit

        val result = useCase(job)

        assertTrue(result is SaveResult.Saved)
        coVerify { repository.save(job) }
    }

    @Test
    fun `returns Duplicate when existing entry found`() = runTest {
        val existing = job.copy(id = UUID.randomUUID(), status = ApplicationStatus.APPLIED)
        coEvery { repository.findDuplicate("Google", "Android Engineer") } returns existing

        val result = useCase(job)

        assertTrue(result is SaveResult.Duplicate)
        assertEquals(existing, (result as SaveResult.Duplicate).existing)
        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `calls findDuplicate with correct company and role`() = runTest {
        coEvery { repository.findDuplicate("Google", "Android Engineer") } returns null
        coEvery { repository.save(any()) } returns Unit

        useCase(job)

        coVerify { repository.findDuplicate("Google", "Android Engineer") }
    }

    @Test
    fun `does not call save when duplicate exists`() = runTest {
        val existing = job.copy(id = UUID.randomUUID())
        coEvery { repository.findDuplicate(any(), any()) } returns existing

        useCase(job)

        coVerify(exactly = 0) { repository.save(any()) }
    }
}
