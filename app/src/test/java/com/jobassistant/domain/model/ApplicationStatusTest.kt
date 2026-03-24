package com.jobassistant.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationStatusTest {

    @Test
    fun `ALL_STATUSES contains exactly 10 values`() {
        assertEquals(10, ALL_STATUSES.size)
    }

    @Test
    fun `ALL_STATUSES equals ACTIVE_PIPELINE plus TERMINAL_STATUSES`() {
        assertEquals(ACTIVE_PIPELINE + TERMINAL_STATUSES, ALL_STATUSES)
    }

    @Test
    fun `ACTIVE_PIPELINE has 7 values`() {
        assertEquals(7, ACTIVE_PIPELINE.size)
    }

    @Test
    fun `TERMINAL_STATUSES has 3 values`() {
        assertEquals(3, TERMINAL_STATUSES.size)
    }

    @Test
    fun `ALL_STATUSES contains all 10 enum values`() {
        val all = ALL_STATUSES.toSet()
        ApplicationStatus.values().forEach { status ->
            assertTrue("$status missing from ALL_STATUSES", all.contains(status))
        }
    }

    @Test
    fun `ACTIVE_PIPELINE does not contain terminal statuses`() {
        TERMINAL_STATUSES.forEach { terminal ->
            assertFalse("$terminal should not be in ACTIVE_PIPELINE", ACTIVE_PIPELINE.contains(terminal))
        }
    }

    @Test
    fun `TERMINAL_STATUSES contains REJECTED WITHDRAWN NO_RESPONSE`() {
        assertTrue(TERMINAL_STATUSES.contains(ApplicationStatus.REJECTED))
        assertTrue(TERMINAL_STATUSES.contains(ApplicationStatus.WITHDRAWN))
        assertTrue(TERMINAL_STATUSES.contains(ApplicationStatus.NO_RESPONSE))
    }

    @Test
    fun `every displayName returns non-blank string`() {
        ApplicationStatus.values().forEach { status ->
            val name = status.displayName()
            assertTrue("displayName for $status is blank", name.isNotBlank())
        }
    }

    @Test
    fun `displayName returns distinct values for all statuses`() {
        val names = ApplicationStatus.values().map { it.displayName() }
        assertEquals("displayName values should be distinct", names.size, names.distinct().size)
    }

    @Test
    fun `specific displayName values are correct`() {
        assertEquals("Interested",   ApplicationStatus.INTERESTED.displayName())
        assertEquals("Applied",      ApplicationStatus.APPLIED.displayName())
        assertEquals("Screening",    ApplicationStatus.SCREENING.displayName())
        assertEquals("Interviewing", ApplicationStatus.INTERVIEWING.displayName())
        assertEquals("Assessment",   ApplicationStatus.ASSESSMENT.displayName())
        assertEquals("Offer Received", ApplicationStatus.OFFER.displayName())
        assertEquals("Accepted",     ApplicationStatus.ACCEPTED.displayName())
        assertEquals("Rejected",     ApplicationStatus.REJECTED.displayName())
        assertEquals("Withdrawn",    ApplicationStatus.WITHDRAWN.displayName())
        assertEquals("No Response",  ApplicationStatus.NO_RESPONSE.displayName())
    }
}
