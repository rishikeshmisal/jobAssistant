package com.jobassistant.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jobassistant.data.db.AppDatabase
import com.jobassistant.data.db.Converters
import com.jobassistant.data.db.entity.CareerInsightsEntity
import kotlinx.coroutines.test.runTest
import net.sqlcipher.database.SupportFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CareerInsightsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CareerInsightsDao

    private fun makeEntity(
        id: String = UUID.randomUUID().toString(),
        generatedDate: Long = System.currentTimeMillis(),
        gaps: List<String> = emptyList(),
        actions: List<String> = emptyList(),
        summary: String = ""
    ) = CareerInsightsEntity(
        id = id,
        generatedDate = generatedDate,
        identifiedGaps = gaps,
        recommendedActions = actions,
        summaryAnalysis = summary
    )

    @Before
    fun createDb() {
        val passphrase = "test_passphrase".toByteArray()
        val factory = SupportFactory(passphrase)
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .openHelperFactory(factory)
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        dao = db.careerInsightsDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsert_and_getLatestAsFlow_returns_most_recent() = runTest {
        val older = makeEntity(generatedDate = 1000L, summary = "Old")
        val newer = makeEntity(generatedDate = 2000L, summary = "New")
        dao.upsert(older)
        dao.upsert(newer)

        dao.getLatestAsFlow().test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("New", result?.summaryAnalysis)
            assertEquals(2000L, result?.generatedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getLatestAsFlow_returns_null_when_empty() = runTest {
        dao.getLatestAsFlow().test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsert_stores_lists_correctly() = runTest {
        val entity = makeEntity(
            gaps = listOf("Gap 1", "Gap 2"),
            actions = listOf("Action A", "Action B"),
            summary = "Summary text"
        )
        dao.upsert(entity)

        dao.getLatestAsFlow().test {
            val result = awaitItem()
            assertEquals(listOf("Gap 1", "Gap 2"), result?.identifiedGaps)
            assertEquals(listOf("Action A", "Action B"), result?.recommendedActions)
            assertEquals("Summary text", result?.summaryAnalysis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsert_with_same_id_updates_entry() = runTest {
        val id = UUID.randomUUID().toString()
        val original = makeEntity(id = id, summary = "Original")
        dao.upsert(original)

        val updated = original.copy(summaryAnalysis = "Updated")
        dao.upsert(updated)

        dao.getLatestAsFlow().test {
            val result = awaitItem()
            assertEquals("Updated", result?.summaryAnalysis)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
