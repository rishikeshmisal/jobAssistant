package com.jobassistant.util

import android.content.Intent
import android.provider.CalendarContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class CalendarIntentHelperTest {

    @Test
    fun `buildInsertIntent returns ACTION_INSERT`() {
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Interview at Acme",
            startMillis = 1_700_000_000_000L,
            description = "Technical interview",
            location = null
        )
        assertEquals(Intent.ACTION_INSERT, intent.action)
    }

    @Test
    fun `buildInsertIntent sets correct EXTRA_EVENT_BEGIN_TIME`() {
        val startMillis = 1_700_000_000_000L
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Interview",
            startMillis = startMillis,
            description = "Phone screen",
            location = null
        )
        assertEquals(startMillis, intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1L))
    }

    @Test
    fun `buildInsertIntent sets EXTRA_EVENT_END_TIME to start plus one hour`() {
        val startMillis = 1_700_000_000_000L
        val expectedEnd = startMillis + 60 * 60 * 1000L
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Interview",
            startMillis = startMillis,
            description = "On-site interview",
            location = null
        )
        assertEquals(expectedEnd, intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1L))
    }

    @Test
    fun `buildInsertIntent sets TITLE extra`() {
        val title = "Interview at Google"
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = title,
            startMillis = 1_700_000_000_000L,
            description = "Final round",
            location = null
        )
        assertEquals(title, intent.getStringExtra(CalendarContract.Events.TITLE))
    }

    @Test
    fun `buildInsertIntent sets DESCRIPTION extra`() {
        val description = "Interview link: https://zoom.us/j/12345"
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Interview",
            startMillis = 1_700_000_000_000L,
            description = description,
            location = null
        )
        assertEquals(description, intent.getStringExtra(CalendarContract.Events.DESCRIPTION))
    }

    @Test
    fun `buildInsertIntent sets EVENT_LOCATION when provided`() {
        val location = "https://meet.google.com/abc-defg-hij"
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Interview",
            startMillis = 1_700_000_000_000L,
            description = "Video interview",
            location = location
        )
        assertEquals(location, intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION))
    }

    @Test
    fun `buildInsertIntent does not set EVENT_LOCATION when null`() {
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Interview",
            startMillis = 1_700_000_000_000L,
            description = "Phone screen",
            location = null
        )
        assertNull(intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION))
    }

    @Test
    fun `buildInsertIntent data URI is CalendarEvents CONTENT_URI`() {
        val intent = CalendarIntentHelper.buildInsertIntent(
            title = "Test",
            startMillis = 1_000_000L,
            description = "desc",
            location = null
        )
        assertNotNull(intent.data)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
    }
}
