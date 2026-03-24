package com.jobassistant.util

/**
 * Pure Kotlin CSV parser — no Android dependencies, fully JVM-testable.
 *
 * Handles:
 * - Quoted fields (fields containing commas wrapped in double-quotes)
 * - Escaped quotes inside quoted fields ("" → ")
 * - Windows (CRLF) and Unix (LF) line endings
 * - Trailing newline — does not produce an extra empty row
 */
object CsvParser {

    data class ParsedCsv(
        val headers: List<String>,
        val rows: List<List<String>>
    )

    /**
     * Parses [csvText] into a [ParsedCsv].
     * Returns null if the text is blank or contains no header row.
     */
    fun parse(csvText: String): ParsedCsv? {
        if (csvText.isBlank()) return null

        // Normalise line endings then split into lines
        val lines = csvText
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trimEnd('\n')
            .split("\n")
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) return null

        val headers = parseLine(lines[0])
        if (headers.isEmpty()) return null

        val rows = lines.drop(1).map { parseLine(it) }
        return ParsedCsv(headers = headers, rows = rows)
    }

    /** Parse a single CSV line, respecting quoted fields and escaped quotes. */
    internal fun parseLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    // Escaped quote inside quoted field
                    current.append('"')
                    i += 2
                }
                ch == '"' -> {
                    inQuotes = !inQuotes
                    i++
                }
                ch == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                    i++
                }
                else -> {
                    current.append(ch)
                    i++
                }
            }
        }
        fields.add(current.toString())
        return fields
    }
}
