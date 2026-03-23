package com.jobassistant.data.db

import com.jobassistant.domain.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Test
    fun `fromStringList and toStringList roundtrip`() {
        val list = listOf("Kotlin", "Android", "Compose")
        val json = converters.fromStringList(list)
        val result = converters.toStringList(json)
        assertEquals(list, result)
    }

    @Test
    fun `toStringList returns empty list for empty json array`() {
        val result = converters.toStringList("[]")
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `fromStringList produces valid json for empty list`() {
        val json = converters.fromStringList(emptyList())
        val result = converters.toStringList(json)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `fromStatus converts enum to name`() {
        for (status in ApplicationStatus.values()) {
            assertEquals(status.name, converters.fromStatus(status))
        }
    }

    @Test
    fun `toStatus converts name to enum`() {
        for (status in ApplicationStatus.values()) {
            assertEquals(status, converters.toStatus(status.name))
        }
    }

    @Test
    fun `fromStatus and toStatus roundtrip for all statuses`() {
        for (status in ApplicationStatus.values()) {
            assertEquals(status, converters.toStatus(converters.fromStatus(status)))
        }
    }

    @Test
    fun `fromStringList serializes single-element list`() {
        val list = listOf("solo")
        val json = converters.fromStringList(list)
        val result = converters.toStringList(json)
        assertEquals(list, result)
    }
}
