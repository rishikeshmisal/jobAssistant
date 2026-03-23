package com.jobassistant.util

import android.content.Intent
import android.provider.CalendarContract

object CalendarIntentHelper {

    fun buildInsertIntent(
        title: String,
        startMillis: Long,
        description: String,
        location: String?
    ): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000L)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
        }
    }
}
