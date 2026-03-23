package com.jobassistant.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jobassistant.data.db.AppDatabase
import com.jobassistant.data.db.Converters
import com.jobassistant.data.db.entity.JobApplicationEntity
import com.jobassistant.domain.model.ApplicationStatus
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
class JobApplicationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: JobApplicationDao

    private fun makeEntity(
        id: String = UUID.randomUUID().toString(),
        company: String = "Test Corp",
        role: String = "Engineer",
        status: ApplicationStatus = ApplicationStatus.SAVED,
        appliedDate: Long? = null
    ) = JobApplicationEntity(
        id = id,
        companyName = company,
        roleTitle = role,
        jobUrl = null,
        status = status,
        fitScore = null,
        location = null,
        salaryRange = null,
        appliedDate = appliedDate,
        interviewDate = null,
        notes = "",
        linkedEmailThreadIds = emptyList(),
        lastSeenDate = System.currentTimeMillis()
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
        dao = db.jobApplicationDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsert_and_getById_roundtrip() = runTest {
        val entity = makeEntity(company = "Acme", role = "Dev")
        dao.upsert(entity)

        val loaded = dao.getById(entity.id)
        assertNotNull(loaded)
        assertEquals("Acme", loaded?.companyName)
        assertEquals("Dev", loaded?.roleTitle)
    }

    @Test
    fun getById_returns_null_for_missing_id() = runTest {
        val result = dao.getById(UUID.randomUUID().toString())
        assertNull(result)
    }

    @Test
    fun getAllAsFlow_emits_upserted_entries() = runTest {
        val e1 = makeEntity(company = "Alpha")
        val e2 = makeEntity(company = "Beta")
        dao.upsert(e1)
        dao.upsert(e2)

        dao.getAllAsFlow().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_removes_entry() = runTest {
        val entity = makeEntity()
        dao.upsert(entity)
        dao.delete(entity)

        val loaded = dao.getById(entity.id)
        assertNull(loaded)
    }

    @Test
    fun findDuplicate_exact_match() = runTest {
        val entity = makeEntity(company = "Google", role = "SWE")
        dao.upsert(entity)

        val result = dao.findDuplicate("Google", "SWE")
        assertNotNull(result)
        assertEquals(entity.id, result?.id)
    }

    @Test
    fun findDuplicate_returns_null_for_no_match() = runTest {
        val entity = makeEntity(company = "Google", role = "SWE")
        dao.upsert(entity)

        val result = dao.findDuplicate("Google", "PM")
        assertNull(result)
    }

    @Test
    fun upsert_updates_existing_entry_on_conflict() = runTest {
        val id = UUID.randomUUID().toString()
        val original = makeEntity(id = id, company = "Corp", role = "Dev", status = ApplicationStatus.SAVED)
        dao.upsert(original)

        val updated = original.copy(status = ApplicationStatus.APPLIED)
        dao.upsert(updated)

        val loaded = dao.getById(id)
        assertEquals(ApplicationStatus.APPLIED, loaded?.status)
    }

    @Test
    fun getByStatusAsFlow_filters_correctly() = runTest {
        dao.upsert(makeEntity(company = "A", status = ApplicationStatus.SAVED))
        dao.upsert(makeEntity(company = "B", status = ApplicationStatus.APPLIED))
        dao.upsert(makeEntity(company = "C", status = ApplicationStatus.APPLIED))

        dao.getByStatusAsFlow(ApplicationStatus.APPLIED.name).test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assert(items.all { it.status == ApplicationStatus.APPLIED })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
