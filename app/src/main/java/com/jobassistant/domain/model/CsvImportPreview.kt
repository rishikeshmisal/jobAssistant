package com.jobassistant.domain.model

/**
 * Result of the AI-assisted CSV parsing step — fully constructed jobs ready to
 * insert, plus metadata about the column mapping used and rows that were skipped.
 */
data class CsvImportPreview(
    /** Fully constructed JobApplication objects ready to insert into Room. */
    val jobs: List<JobApplication>,
    /** The column mapping Gemini detected and applied. */
    val columnMapping: CsvColumnMapping,
    /** Total data rows in the CSV file (excluding the header row). */
    val totalRows: Int,
    /** Rows skipped because companyName or roleTitle could not be extracted. */
    val skippedRows: Int
)
