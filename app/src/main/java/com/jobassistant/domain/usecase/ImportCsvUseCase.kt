package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.safeClaudeCall
import com.jobassistant.data.repository.ClaudeRepository
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.CsvColumnMapping
import com.jobassistant.domain.model.CsvImportPreview
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.util.CsvParser
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private val DATE_FALLBACK_PATTERNS = listOf(
    "yyyy-MM-dd",
    "dd/MM/yyyy",
    "MM/dd/yyyy",
    "d MMM yyyy",
    "MMM d, yyyy",
    "d-MMM-yy",
    "yyyy/MM/dd"
)

class ImportCsvUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase
) {

    /** Parses [csvText] and calls Gemini to map columns → returns a preview for user confirmation. */
    suspend fun preview(csvText: String): ClaudeResult<CsvImportPreview> = safeClaudeCall {
        val parsed = CsvParser.parse(csvText)
            ?: throw IllegalArgumentException("CSV file is empty or has no header row")

        val sampleRows = parsed.rows.take(5)
        val mapping = claudeRepository.mapCsvColumns(parsed.headers, sampleRows)

        buildPreview(parsed, mapping)
    }

    /** Inserts all jobs from [preview] into Room; duplicate rows are silently skipped. */
    suspend fun commit(preview: CsvImportPreview): Pair<Int, Int> {
        var imported = 0
        var duplicates = 0
        for (job in preview.jobs) {
            when (saveJobApplicationUseCase(job)) {
                is SaveResult.Saved    -> imported++
                is SaveResult.Duplicate -> duplicates++
            }
        }
        return Pair(imported, duplicates)
    }

    // ── Internal mapping logic ────────────────────────────────────────────────

    private fun buildPreview(
        parsed: CsvParser.ParsedCsv,
        mapping: CsvColumnMapping
    ): CsvImportPreview {
        // Build a column-index lookup: dbField → column index in parsed.headers
        val fieldIndex: Map<String, Int> = parsed.headers.mapIndexedNotNull { idx, header ->
            val dbField = mapping.columnMappings[header] ?: return@mapIndexedNotNull null
            if (dbField == "IGNORE") null else dbField to idx
        }.toMap()

        val jobs = mutableListOf<JobApplication>()
        var skipped = 0

        for (row in parsed.rows) {
            val companyName = fieldIndex["companyName"]?.let { row.getOrNull(it)?.trim() }
            val roleTitle   = fieldIndex["roleTitle"]?.let { row.getOrNull(it)?.trim() }

            if (companyName.isNullOrBlank() || roleTitle.isNullOrBlank()) {
                skipped++
                continue
            }

            val rawStatus = fieldIndex["status"]?.let { row.getOrNull(it)?.trim() }
            val status = rawStatus?.let { mapStatus(it, mapping) } ?: ApplicationStatus.APPLIED

            val rawDate = fieldIndex["appliedDate"]?.let { row.getOrNull(it)?.trim() }
            val appliedDate = rawDate?.let { parseDate(it, mapping.datePattern) }

            val fitScore = fieldIndex["fitScore"]
                ?.let { row.getOrNull(it)?.trim()?.toIntOrNull() }

            val location    = fieldIndex["location"]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val salaryRange = fieldIndex["salaryRange"]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val notes       = fieldIndex["notes"]?.let { row.getOrNull(it)?.trim() } ?: ""

            jobs.add(
                JobApplication(
                    id = UUID.randomUUID(),
                    companyName = companyName,
                    roleTitle = roleTitle,
                    status = status,
                    fitScore = fitScore,
                    location = location,
                    salaryRange = salaryRange,
                    appliedDate = appliedDate,
                    notes = notes,
                    lastSeenDate = System.currentTimeMillis()
                )
            )
        }

        return CsvImportPreview(
            jobs = jobs,
            columnMapping = mapping,
            totalRows = parsed.rows.size,
            skippedRows = skipped
        )
    }

    private fun mapStatus(raw: String, mapping: CsvColumnMapping): ApplicationStatus {
        val mapped = mapping.statusMappings[raw]
            ?: mapping.statusMappings.entries.firstOrNull { it.key.equals(raw, ignoreCase = true) }?.value
        return when (mapped?.uppercase()) {
            "INTERESTED"   -> ApplicationStatus.INTERESTED
            "APPLIED"      -> ApplicationStatus.APPLIED
            "SCREENING"    -> ApplicationStatus.SCREENING
            "INTERVIEWING" -> ApplicationStatus.INTERVIEWING
            "ASSESSMENT"   -> ApplicationStatus.ASSESSMENT
            "OFFER"        -> ApplicationStatus.OFFER
            "ACCEPTED"     -> ApplicationStatus.ACCEPTED
            "REJECTED"     -> ApplicationStatus.REJECTED
            "WITHDRAWN"    -> ApplicationStatus.WITHDRAWN
            "NO_RESPONSE"  -> ApplicationStatus.NO_RESPONSE
            else           -> ApplicationStatus.APPLIED   // default for unmapped values
        }
    }

    private fun parseDate(raw: String, detectedPattern: String?): Long? {
        if (raw.isBlank()) return null
        val patterns = buildList {
            detectedPattern?.let { add(it) }
            addAll(DATE_FALLBACK_PATTERNS)
        }.distinct()

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }
                return sdf.parse(raw)?.time
            } catch (_: ParseException) {
                // try next pattern
            } catch (_: IllegalArgumentException) {
                // invalid pattern string — skip
            }
        }
        return null
    }
}
