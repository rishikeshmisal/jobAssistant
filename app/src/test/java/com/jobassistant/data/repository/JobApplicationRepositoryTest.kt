package com.jobassistant.data.repository

import com.jobassistant.data.db.dao.JobApplicationDao
import com.jobassistant.data.db.entity.JobApplicationEntity
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class JobApplicationRepositoryTest {

    private lateinit var dao: JobApplicationDao
    private lateinit var repository: JobApplicationRepositoryImpl

    private val sampleId = UUID.randomUUID()
    private val sampleEntity = JobApplicationEntity(
        id = sampleId.toString(),
        companyName = "Acme Corp",
        roleTitle = "Software Engineer",
        jobUrl = null,
        status = ApplicationStatus.INTERESTED,
        fitScore = null,
        location = null,
        salaryRange = null,
        appliedDate = null,
        interviewDate = null,
        notes = "",
        linkedEmailThreadIds = emptyList(),
        lastSeenDate = 1000L
    )
    private val sampleJob = JobApplication(
        id = sampleId,
        companyName = "Acme Corp",
        roleTitle = "Software Engineer",
        lastSeenDate = 1000L
    )

    @Before
    fun setUp() {
        dao = mockk()
        repository = JobApplicationRepositoryImpl(dao)
    }

    @Test
    fun `save calls findDuplicate before upsert`() = runTest {
        coEvery { dao.findDuplicate(any(), any()) } returns null
        coEvery { dao.upsert(any()) } returns Unit

        repository.save(sampleJob)

        coVerify { dao.findDuplicate("Acme Corp", "Software Engineer") }
        coVerify { dao.upsert(any()) }
    }

    @Test
    fun `save with duplicate updates existing entry instead of inserting new one`() = runTest {
        val existingEntity = sampleEntity.copy(id = UUID.randomUUID().toString())
        coEvery { dao.findDuplicate(any(), any()) } returns existingEntity
        coEvery { dao.upsert(any()) } returns Unit

        repository.save(sampleJob)

        // Should upsert with the EXISTING entity's id
        coVerify {
            dao.upsert(withArg { upserted ->
                assertEquals(existingEntity.id, upserted.id)
            })
        }
    }

    @Test
    fun `getById maps entity to domain model correctly`() = runTest {
        coEvery { dao.getById(sampleId.toString()) } returns sampleEntity

        val result = repository.getById(sampleId)

        assertEquals(sampleId, result?.id)
        assertEquals("Acme Corp", result?.companyName)
        assertEquals("Software Engineer", result?.roleTitle)
        assertEquals(ApplicationStatus.INTERESTED, result?.status)
    }

    @Test
    fun `getById returns null when entity not found`() = runTest {
        coEvery { dao.getById(any()) } returns null

        val result = repository.getById(UUID.randomUUID())

        assertNull(result)
    }

    @Test
    fun `getAllAsFlow maps entities to domain models`() = runTest {
        every { dao.getAllAsFlow() } returns flowOf(listOf(sampleEntity))

        val jobs = repository.getAllAsFlow().first()

        assertEquals(1, jobs.size)
        assertEquals(sampleId, jobs[0].id)
        assertEquals("Acme Corp", jobs[0].companyName)
    }

    @Test
    fun `findDuplicate returns domain model when match found`() = runTest {
        coEvery { dao.findDuplicate("Acme Corp", "Software Engineer") } returns sampleEntity

        val result = repository.findDuplicate("Acme Corp", "Software Engineer")

        assertEquals(sampleId, result?.id)
    }

    @Test
    fun `findDuplicate returns null when no match`() = runTest {
        coEvery { dao.findDuplicate(any(), any()) } returns null

        val result = repository.findDuplicate("Unknown", "Role")

        assertNull(result)
    }

    @Test
    fun `getByStatusAsFlow maps entities with correct status`() = runTest {
        every { dao.getByStatusAsFlow(ApplicationStatus.APPLIED.name) } returns
            flowOf(listOf(sampleEntity.copy(status = ApplicationStatus.APPLIED)))

        val result = repository.getByStatusAsFlow(ApplicationStatus.APPLIED).first()

        assertEquals(1, result.size)
        assertEquals(ApplicationStatus.APPLIED, result[0].status)
    }

    @Test
    fun `delete delegates to dao`() = runTest {
        coEvery { dao.delete(any()) } returns Unit

        repository.delete(sampleJob)

        coVerify { dao.delete(any()) }
    }
}
