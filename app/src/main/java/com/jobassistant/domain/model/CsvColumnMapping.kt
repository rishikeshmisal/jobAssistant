package com.jobassistant.domain.model

/**
 * AI-generated mapping from CSV column headers to database field names, plus
 * status value normalisation and detected date format.
 */
data class CsvColumnMapping(
    /**
     * Maps each CSV column header to a db field name, or "IGNORE" to skip it.
     * DB field names: companyName, roleTitle, status, appliedDate,
     *                 location, salaryRange, notes, fitScore, IGNORE
     */
    val columnMappings: Map<String, String>,
    /**
     * Maps raw status string values found in the CSV to ApplicationStatus enum names.
     * e.g. "Applied" → "APPLIED", "Phone Screen" → "INTERVIEWING"
     */
    val statusMappings: Map<String, String>,
    /**
     * Java SimpleDateFormat pattern detected in the date column,
     * e.g. "yyyy-MM-dd", "MM/dd/yyyy". Null if no date column was detected.
     */
    val datePattern: String?
)
