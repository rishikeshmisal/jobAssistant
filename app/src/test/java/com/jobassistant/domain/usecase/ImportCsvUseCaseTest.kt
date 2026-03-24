package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.ClaudeRepository
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.CsvColumnMapping
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportCsvUseCaseTest {

    private lateinit var claudeRepository: ClaudeRepository
    private lateinit var saveJobApplicationUseCase: SaveJobApplicationUseCase
    private lateinit var useCase: ImportCsvUseCase

    // Simple mapping: Company→companyName, Role→roleTitle, Status→status, Date→appliedDate
    private val simpleMapping = CsvColumnMapping(
        columnMappings = mapOf(
            "Company" to "companyName",
            "Role"    to "roleTitle",
            "Status"  to "status",
            "Date"    to "appliedDate"
        ),
        statusMappings = mapOf(
            "Applied"  to "APPLIED",
            "Rejected" to "REJECTED"
        ),
        datePattern = "yyyy-MM-dd"
    )

    @Before
    fun setUp() {
        claudeRepository = mockk()
        saveJobApplicationUseCase = mockk(relaxed = true)
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved
        useCase = ImportCsvUseCase(claudeRepository, saveJobApplicationUseCase)
    }

    // ── preview() ─────────────────────────────────────────────────────────────

    @Test
    fun `preview maps companyName and roleTitle from CSV row`() = runTest {
        val csv = "Company,Role,Status\nGoogle,Android Engineer,Applied"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Status" to "status")
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertEquals("Google", result.data.jobs[0].companyName)
        assertEquals("Android Engineer", result.data.jobs[0].roleTitle)
    }

    @Test
    fun `preview skips row with blank companyName`() = runTest {
        val csv = "Company,Role\n,Engineer\nMeta,PM"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle"),
            statusMappings = emptyMap(), datePattern = null
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertEquals(1, result.data.jobs.size)
        assertEquals(1, result.data.skippedRows)
        assertEquals("Meta", result.data.jobs[0].companyName)
    }

    @Test
    fun `preview skips row with blank roleTitle`() = runTest {
        val csv = "Company,Role\nGoogle,\nMeta,PM"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle"),
            statusMappings = emptyMap(), datePattern = null
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertEquals(1, result.data.jobs.size)
        assertEquals(1, result.data.skippedRows)
    }

    @Test
    fun `preview maps status via statusMappings`() = runTest {
        val csv = "Company,Role,Status\nGoogle,SWE,Rejected"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Status" to "status"),
            datePattern = null
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertEquals(ApplicationStatus.REJECTED, result.data.jobs[0].status)
    }

    @Test
    fun `preview defaults to APPLIED for unknown status value`() = runTest {
        val csv = "Company,Role,Status\nGoogle,SWE,Ghosted"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Status" to "status"),
            statusMappings = mapOf("Applied" to "APPLIED"), // no mapping for "Ghosted"
            datePattern = null
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertEquals(ApplicationStatus.APPLIED, result.data.jobs[0].status)
    }

    @Test
    fun `preview parses date with detected datePattern`() = runTest {
        val csv = "Company,Role,Date\nGoogle,SWE,2024-03-15"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Date" to "appliedDate"),
            statusMappings = emptyMap(), datePattern = "yyyy-MM-dd"
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        val job = result.data.jobs[0]
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = job.appliedDate!! }
        assertEquals(2024, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.MARCH, cal.get(java.util.Calendar.MONTH))
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `preview falls back to alternative date patterns when detected fails`() = runTest {
        val csv = "Company,Role,Date\nGoogle,SWE,15/03/2024"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Date" to "appliedDate"),
            statusMappings = emptyMap(), datePattern = "yyyy-MM-dd" // wrong pattern → fallback to dd/MM/yyyy
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        val job = result.data.jobs[0]
        // Should parse via dd/MM/yyyy fallback
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = job.appliedDate!! }
        assertEquals(2024, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.MARCH, cal.get(java.util.Calendar.MONTH))
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `preview parses fitScore as Int when valid`() = runTest {
        val csv = "Company,Role,Score\nGoogle,SWE,85"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Score" to "fitScore"),
            statusMappings = emptyMap(), datePattern = null
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertEquals(85, result.data.jobs[0].fitScore)
    }

    @Test
    fun `preview stores null fitScore for non-integer value`() = runTest {
        val csv = "Company,Role,Score\nGoogle,SWE,N/A"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle", "Score" to "fitScore"),
            statusMappings = emptyMap(), datePattern = null
        )

        val result = useCase.preview(csv) as ClaudeResult.Success
        assertNull(result.data.jobs[0].fitScore)
    }

    @Test
    fun `preview returns error for empty CSV`() = runTest {
        val result = useCase.preview("")
        assertTrue(result is ClaudeResult.Error)
    }

    @Test
    fun `preview sends only first 5 sample rows to Gemini`() = runTest {
        val header = "Company,Role"
        val dataRows = (1..10).joinToString("\n") { "Company$it,Role$it" }
        val csv = "$header\n$dataRows"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle"),
            statusMappings = emptyMap(), datePattern = null
        )

        useCase.preview(csv)

        coVerify { claudeRepository.mapCsvColumns(any(), match { it.size <= 5 }) }
    }

    // ── commit() ──────────────────────────────────────────────────────────────

    @Test
    fun `commit calls SaveJobApplicationUseCase for each job`() = runTest {
        val csv = "Company,Role\nGoogle,SWE\nMeta,PM"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle"),
            statusMappings = emptyMap(), datePattern = null
        )
        val preview = (useCase.preview(csv) as ClaudeResult.Success).data
        coEvery { saveJobApplicationUseCase(any()) } returns SaveResult.Saved

        val (imported, duplicates) = useCase.commit(preview)

        assertEquals(2, imported)
        assertEquals(0, duplicates)
    }

    @Test
    fun `commit counts duplicates silently`() = runTest {
        val csv = "Company,Role\nGoogle,SWE\nMeta,PM"
        coEvery { claudeRepository.mapCsvColumns(any(), any()) } returns simpleMapping.copy(
            columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle"),
            statusMappings = emptyMap(), datePattern = null
        )
        val preview = (useCase.preview(csv) as ClaudeResult.Success).data
        val existingJob = com.jobassistant.domain.model.JobApplication(
            companyName = "Google", roleTitle = "SWE"
        )
        coEvery { saveJobApplicationUseCase(match { it.companyName == "Google" }) } returns SaveResult.Duplicate(existingJob)
        coEvery { saveJobApplicationUseCase(match { it.companyName == "Meta" }) } returns SaveResult.Saved

        val (imported, duplicates) = useCase.commit(preview)

        assertEquals(1, imported)
        assertEquals(1, duplicates)
    }
}
