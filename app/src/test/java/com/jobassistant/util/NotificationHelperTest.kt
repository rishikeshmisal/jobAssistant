package com.jobassistant.util

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class NotificationHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `showJobAlert creates notification channel with correct ID`() {
        NotificationHelper.showJobAlert(context, "Test Title", "Test Body")

        val notificationManager = context.getSystemService<NotificationManager>()!!
        val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(NotificationHelper.CHANNEL_ID, channel!!.id)
    }

    @Test
    fun `notification channel has IMPORTANCE_HIGH`() {
        NotificationHelper.showJobAlert(context, "Alert", "Body")

        val notificationManager = context.getSystemService<NotificationManager>()!!
        val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel!!.importance)
    }

    @Test
    fun `showJobAlert does not throw when called multiple times`() {
        // Channel creation is idempotent — calling multiple times should be safe
        repeat(3) {
            NotificationHelper.showJobAlert(context, "Title $it", "Body $it")
        }

        val notificationManager = context.getSystemService<NotificationManager>()!!
        val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
        assertNotNull(channel)
    }

    @Test
    fun `channel ID constant is job_alerts`() {
        assertEquals("job_alerts", NotificationHelper.CHANNEL_ID)
    }
}
