package com.aswinas.caitlin.planner.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Scheduled Timetable Event"
        val timeLabel = intent.getStringExtra(NotificationHelper.EXTRA_TIME_LABEL) ?: ""
        val category = intent.getStringExtra(NotificationHelper.EXTRA_CATEGORY) ?: "Focus"
        val slotId = intent.getStringExtra(NotificationHelper.EXTRA_SLOT_ID) ?: ""

        val notificationTitle = "Timetable Alert: $title"
        val notificationMessage = "Starting now ($timeLabel) • Category: $category"
        val notificationId = if (slotId.isNotEmpty()) slotId.hashCode() else 7701

        NotificationHelper.sendNotification(
            context = context,
            title = notificationTitle,
            message = notificationMessage,
            notificationId = notificationId,
            channelId = NotificationHelper.CHANNEL_TIMETABLE
        )
    }
}
