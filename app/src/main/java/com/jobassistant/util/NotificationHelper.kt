package com.jobassistant.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

object NotificationHelper {

    const val CHANNEL_ID = "job_alerts"
    private const val CHANNEL_NAME = "Job Alerts"
    private const val NOTIFICATION_ID_BASE = 1000

    fun showJobAlert(context: Context, title: String, body: String) {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        ensureChannelCreated(notificationManager)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + title.hashCode(), notification)
    }

    private fun ensureChannelCreated(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for job application alerts and interview updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
