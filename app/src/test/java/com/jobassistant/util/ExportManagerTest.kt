package com.jobassistant.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class ExportManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val gson = GsonBuilder().create()
    private lateinit var exportManager: ExportManager

    @Before
    fun setUp() {
        exportManager = ExportManager(gson)
    }

    @Test
    fun `exportToJson round-trips companyName correctly`() = runTest {
        val jobs = listOf(
            JobApplication(companyName = "Acme Corp", roleTitle = "Android Engineer")
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals("Acme Corp", parsed[0].companyName)
    }

    @Test
    fun `exportToJson round-trips roleTitle correctly`() = runTest {
        val jobs = listOf(
            JobApplication(companyName = "Globex", roleTitle = "Senior Engineer")
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals("Senior Engineer", parsed[0].roleTitle)
    }

    @Test
    fun `exportToJson round-trips status correctly`() = runTest {
        val jobs = listOf(
            JobApplication(
                companyName = "Initech",
                roleTitle = "Developer",
                status = ApplicationStatus.INTERVIEWING
            )
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals(ApplicationStatus.INTERVIEWING, parsed[0].status)
    }

    @Test
    fun `exportToJson round-trips fitScore correctly`() = runTest {
        val jobs = listOf(
            JobApplication(
                companyName = "Umbrella",
                roleTitle = "Engineer",
                fitScore = 85
            )
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals(85, parsed[0].fitScore)
    }

    @Test
    fun `exportToJson round-trips nullable fields correctly`() = runTest {
        val jobs = listOf(
            JobApplication(
                companyName = "Stark Industries",
                roleTitle = "Robotics Lead",
                jobUrl = "https://jobs.stark.com/123",
                location = "New York",
                salaryRange = "150k-200k",
                notes = "Interesting role",
                fitScore = null
            )
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals("https://jobs.stark.com/123", parsed[0].jobUrl)
        assertEquals("New York", parsed[0].location)
        assertEquals("150k-200k", parsed[0].salaryRange)
        assertEquals("Interesting role", parsed[0].notes)
        assertEquals(null, parsed[0].fitScore)
    }

    @Test
    fun `exportToJson round-trips all jobs in list`() = runTest {
        val jobs = listOf(
            JobApplication(companyName = "Company A", roleTitle = "Role A"),
            JobApplication(companyName = "Company B", roleTitle = "Role B"),
            JobApplication(companyName = "Company C", roleTitle = "Role C")
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals(3, parsed.size)
        assertEquals("Company A", parsed[0].companyName)
        assertEquals("Company B", parsed[1].companyName)
        assertEquals("Company C", parsed[2].companyName)
    }

    @Test
    fun `exportToJson creates valid file at returned URI`() = runTest {
        val jobs = listOf(JobApplication(companyName = "TestCo", roleTitle = "Dev"))
        val uri = exportManager.exportToJson(context, jobs)
        assertNotNull(uri.path)
        assertTrue("Expected file to exist at ${uri.path}", File(uri.path!!).exists())
    }

    @Test
    fun `exportToJson filename contains today date in YYYYMMDD format`() = runTest {
        val jobs = listOf(JobApplication(companyName = "DateTest", roleTitle = "Dev"))
        val uri = exportManager.exportToJson(context, jobs)
        val expectedDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        assertTrue(
            "Expected filename to contain $expectedDate but was ${uri.path}",
            uri.path!!.contains(expectedDate)
        )
    }

    @Test
    fun `exportToJson filename follows expected pattern`() = runTest {
        val jobs = emptyList<JobApplication>()
        val uri = exportManager.exportToJson(context, jobs)
        val fileName = File(uri.path!!).name
        assertTrue(
            "Expected filename to start with 'jobassistant_backup_' but was $fileName",
            fileName.startsWith("jobassistant_backup_")
        )
        assertTrue(
            "Expected filename to end with '.json' but was $fileName",
            fileName.endsWith(".json")
        )
    }

    @Test
    fun `exportToJson handles empty list without error`() = runTest {
        val uri = exportManager.exportToJson(context, emptyList())
        val parsed = parseExportFile(uri.path!!)
        assertEquals(0, parsed.size)
    }

    @Test
    fun `exportToJson round-trips UUID id correctly`() = runTest {
        val knownId = UUID.randomUUID()
        val jobs = listOf(
            JobApplication(id = knownId, companyName = "IdTest", roleTitle = "Dev")
        )
        val uri = exportManager.exportToJson(context, jobs)
        val parsed = parseExportFile(uri.path!!)
        assertEquals(knownId, parsed[0].id)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun parseExportFile(path: String): List<JobApplication> {
        val text = File(path).readText()
        val type = object : TypeToken<List<JobApplication>>() {}.type
        return ExportManager(gson).run {
            // Re-use the same ExportManager gson to deserialize so the UUID adapter matches
            val exportGsonField = ExportManager::class.java.getDeclaredField("exportGson")
            exportGsonField.isAccessible = true
            val exportGson = exportGsonField.get(this) as com.google.gson.Gson
            exportGson.fromJson(text, type)
        }
    }
}
