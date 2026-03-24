package com.jobassistant.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvParserTest {

    // ── parse() ───────────────────────────────────────────────────────────────

    @Test
    fun `blank text returns null`() {
        assertNull(CsvParser.parse(""))
        assertNull(CsvParser.parse("   "))
    }

    @Test
    fun `standard csv parses headers and rows correctly`() {
        val csv = "Company,Role,Status\nGoogle,SWE,Applied\nMeta,PM,Rejected"
        val result = CsvParser.parse(csv)!!
        assertEquals(listOf("Company", "Role", "Status"), result.headers)
        assertEquals(2, result.rows.size)
        assertEquals(listOf("Google", "SWE", "Applied"), result.rows[0])
        assertEquals(listOf("Meta", "PM", "Rejected"), result.rows[1])
    }

    @Test
    fun `header-only file returns ParsedCsv with empty rows`() {
        val csv = "Company,Role,Status"
        val result = CsvParser.parse(csv)!!
        assertEquals(listOf("Company", "Role", "Status"), result.headers)
        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun `trailing newline does not produce extra empty row`() {
        val csv = "Company,Role\nGoogle,SWE\n"
        val result = CsvParser.parse(csv)!!
        assertEquals(1, result.rows.size)
    }

    @Test
    fun `windows CRLF line endings are handled`() {
        val csv = "Company,Role\r\nGoogle,SWE\r\nMeta,PM"
        val result = CsvParser.parse(csv)!!
        assertEquals(2, result.rows.size)
        assertEquals("Google", result.rows[0][0])
        assertEquals("Meta", result.rows[1][0])
    }

    // ── Quoted fields ─────────────────────────────────────────────────────────

    @Test
    fun `quoted field containing comma parses as single field`() {
        val csv = "Company,Role\n\"Acme, Corp\",Engineer"
        val result = CsvParser.parse(csv)!!
        assertEquals("Acme, Corp", result.rows[0][0])
        assertEquals("Engineer", result.rows[0][1])
    }

    @Test
    fun `escaped quotes inside quoted field become single quote`() {
        val csv = "Company,Notes\nGoogle,\"He said \"\"hello\"\"\""
        val result = CsvParser.parse(csv)!!
        assertEquals("He said \"hello\"", result.rows[0][1])
    }

    @Test
    fun `quoted field with no special content strips quotes`() {
        val csv = "Company,Role\n\"Google\",\"Engineer\""
        val result = CsvParser.parse(csv)!!
        assertEquals("Google", result.rows[0][0])
        assertEquals("Engineer", result.rows[0][1])
    }

    @Test
    fun `empty quoted field becomes empty string`() {
        val csv = "Company,Role,Notes\nGoogle,SWE,\"\""
        val result = CsvParser.parse(csv)!!
        assertEquals("", result.rows[0][2])
    }

    // ── parseLine() ───────────────────────────────────────────────────────────

    @Test
    fun `parseLine splits simple line`() {
        val fields = CsvParser.parseLine("a,b,c")
        assertEquals(listOf("a", "b", "c"), fields)
    }

    @Test
    fun `parseLine handles trailing comma`() {
        val fields = CsvParser.parseLine("a,b,")
        assertEquals(3, fields.size)
        assertEquals("", fields[2])
    }

    @Test
    fun `parseLine handles single field`() {
        val fields = CsvParser.parseLine("hello")
        assertEquals(listOf("hello"), fields)
    }
}
