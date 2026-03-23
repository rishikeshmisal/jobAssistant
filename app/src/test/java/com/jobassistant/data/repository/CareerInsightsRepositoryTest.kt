package com.jobassistant.data.repository

import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.entity.CareerInsightsEntity
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

class CareerInsightsRepositoryTest {

    private lateinit var dao: CareerInsightsDao
    private lateinit var repository: CareerInsightsRepositoryImpl

    private val sampleId = UUID.randomUUID()
    private val sampleEntity = CareerInsightsEntity(
        id = sampleId.toString(),
        generatedDate = 1000L,
        identifiedGaps = listOf("Gap A"),
        recommendedActions = listOf("Action 1"),
        summaryAnalysis = "Summary"
    )

    @Before
    fun setUp() {
        dao = mockk()
        repository = CareerInsightsRepositoryImpl(dao)
    }

    @Test
    fun `save calls upsert on dao`() = runTest {
        coEvery { dao.upsert(any()) } returns Unit

        val insights = com.jobassistant.domain.model.CareerInsights(
            id = sampleId,
            generatedDate = 1000L,
            identifiedGaps = listOf("Gap A"),
            recommendedActions = listOf("Action 1"),
            summaryAnalysis = "Summary"
        )
        repository.save(insights)

        coVerify { dao.upsert(any()) }
    }

    @Test
    fun `getLatestAsFlow maps entity to domain model`() = runTest {
        every { dao.getLatestAsFlow() } returns flowOf(sampleEntity)

        val result = repository.getLatestAsFlow().first()

        assertEquals(sampleId, result?.id)
        assertEquals(1000L, result?.generatedDate)
        assertEquals(listOf("Gap A"), result?.identifiedGaps)
        assertEquals("Summary", result?.summaryAnalysis)
    }

    @Test
    fun `getLatestAsFlow emits null when no insights stored`() = runTest {
        every { dao.getLatestAsFlow() } returns flowOf(null)

        val result = repository.getLatestAsFlow().first()

        assertNull(result)
    }

    @Test
    fun `save correctly maps domain model to entity`() = runTest {
        coEvery { dao.upsert(any()) } returns Unit

        val insights = com.jobassistant.domain.model.CareerInsights(
            id = sampleId,
            generatedDate = 2000L,
            identifiedGaps = listOf("Gap 1", "Gap 2"),
            recommendedActions = listOf("Do A", "Do B"),
            summaryAnalysis = "New Summary"
        )
        repository.save(insights)

        coVerify {
            dao.upsert(withArg { entity ->
                assertEquals(sampleId.toString(), entity.id)
                assertEquals(2000L, entity.generatedDate)
                assertEquals(listOf("Gap 1", "Gap 2"), entity.identifiedGaps)
            })
        }
    }
}
