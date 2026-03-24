package com.jobassistant.ui.components

import com.jobassistant.domain.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ── RelativeTimeText ──────────────────────────────────────────────────────────

class RelativeTimeTextTest {

    private val now = 1_700_000_000_000L  // fixed reference epoch

    @Test
    fun `null returns em dash`() {
        assertEquals("—", relativeTimeString(null, now))
    }

    @Test
    fun `30 minutes ago returns just now`() {
        assertEquals("just now", relativeTimeString(now - 30 * 60 * 1000L, now))
    }

    @Test
    fun `3 hours ago returns hours ago string`() {
        assertEquals("3 hours ago", relativeTimeString(now - 3 * 60 * 60 * 1000L, now))
    }

    @Test
    fun `1 hour uses singular form`() {
        assertEquals("1 hour ago", relativeTimeString(now - 1 * 60 * 60 * 1000L, now))
    }

    @Test
    fun `3 days ago returns days ago string`() {
        assertEquals("3 days ago", relativeTimeString(now - 3 * 24 * 60 * 60 * 1000L, now))
    }

    @Test
    fun `1 day uses singular form`() {
        assertEquals("1 day ago", relativeTimeString(now - 1 * 24 * 60 * 60 * 1000L, now))
    }

    @Test
    fun `8 days ago returns formatted date not ago string`() {
        val result = relativeTimeString(now - 8 * 24 * 60 * 60 * 1000L, now)
        assertTrue("Expected date format but got: $result", !result.contains("ago"))
    }

    @Test
    fun `future timestamp returns formatted date`() {
        val result = relativeTimeString(now + 1 * 24 * 60 * 60 * 1000L, now)
        assertTrue("Expected date format but got: $result", !result.contains("ago"))
    }
}

// ── FitScoreRing sweep calculation ───────────────────────────────────────────

class FitScoreRingSweepTest {

    @Test
    fun `null score returns 0 sweep`() {
        assertEquals(0f, fitScoreToSweep(null))
    }

    @Test
    fun `score 0 returns 0 sweep`() {
        assertEquals(0f, fitScoreToSweep(0))
    }

    @Test
    fun `score 50 returns half of 270 degrees`() {
        assertEquals(135f, fitScoreToSweep(50))
    }

    @Test
    fun `score 100 returns full 270 degrees`() {
        assertEquals(270f, fitScoreToSweep(100))
    }

    @Test
    fun `score 25 returns quarter of 270 degrees`() {
        assertEquals(67.5f, fitScoreToSweep(25))
    }

    @Test
    fun `score 75 returns three quarters of 270 degrees`() {
        assertEquals(202.5f, fitScoreToSweep(75))
    }
}

// ── StatusChip colors ─────────────────────────────────────────────────────────

class StatusChipColorTest {

    @Test
    fun `all statuses have distinct container colors`() {
        val colors = ApplicationStatus.values().map { statusContainerColor(it) }
        assertEquals(
            "All status container colors should be distinct",
            colors.size,
            colors.distinct().size
        )
    }

    @Test
    fun `all statuses have distinct label colors`() {
        val colors = ApplicationStatus.values().map { statusLabelColor(it) }
        assertEquals(
            "All status label colors should be distinct",
            colors.size,
            colors.distinct().size
        )
    }

    @Test
    fun `container and label color differ for every status`() {
        ApplicationStatus.values().forEach { status ->
            assertNotEquals(
                "Container and label color should differ for $status",
                statusContainerColor(status),
                statusLabelColor(status)
            )
        }
    }
}

// ── CompanyAvatar letter extraction ──────────────────────────────────────────

class CompanyAvatarLetterTest {

    @Test
    fun `normal company name returns uppercase first letter`() {
        assertEquals("G", companyInitial("Google"))
    }

    @Test
    fun `lowercase first letter is uppercased`() {
        assertEquals("A", companyInitial("apple"))
    }

    @Test
    fun `empty string returns question mark`() {
        assertEquals("?", companyInitial(""))
    }

    @Test
    fun `single character returns it uppercased`() {
        assertEquals("X", companyInitial("x"))
    }

    @Test
    fun `already uppercase unchanged`() {
        assertEquals("M", companyInitial("Meta"))
    }

    @Test
    fun `numeric first char is treated as initial`() {
        assertEquals("3", companyInitial("3M"))
    }
}
