package com.aswinas.caitlin.planner.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aswinas.caitlin.planner.MainActivity
import com.aswinas.caitlin.planner.R
import java.util.Calendar
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_TIMETABLE = "timetable_alerts_channel"
    const val CHANNEL_TASKS = "task_reminders_channel"

    private const val ACTION_TIMETABLE_ALERT = "com.aswinas.caitlin.planner.action.TIMETABLE_ALERT"
    const val EXTRA_SLOT_ID = "extra_slot_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_TIME_LABEL = "extra_time_label"
    const val EXTRA_CATEGORY = "extra_category"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val timetableChannel = NotificationChannel(
                CHANNEL_TIMETABLE,
                "Timetable Event Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timetable event start alerts and chrono-block reminders"
                enableVibration(true)
            }

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task & Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily task reminders and unfinished task notifications"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(timetableChannel)
            notificationManager.createNotificationChannel(taskChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun scheduleTimetableAlert(
        context: Context,
        slotId: String,
        timeLabel: String,
        eventTitle: String,
        category: String
    ): Boolean {
        createNotificationChannels(context)
        val triggerMillis = parseTriggerTimeMillis(timeLabel) ?: return false

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TIMETABLE_ALERT
            putExtra(EXTRA_SLOT_ID, slotId)
            putExtra(EXTRA_TITLE, eventTitle)
            putExtra(EXTRA_TIME_LABEL, timeLabel)
            putExtra(EXTRA_CATEGORY, category)
        }

        val requestCode = slotId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun cancelTimetableAlert(context: Context, slotId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TIMETABLE_ALERT
        }
        val requestCode = slotId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        channelId: String = CHANNEL_TIMETABLE
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }

    fun sendYesterdayReminderNotification(
        context: Context,
        incompleteCount: Int,
        sampleTitle: String
    ) {
        val title = if (incompleteCount == 1) {
            "Reminder: Yesterday's Task Incomplete"
        } else {
            "Reminder: $incompleteCount Incomplete Tasks from Yesterday"
        }
        val message = "Keep your momentum going! Finish or reschedule \"$sampleTitle\" in your Daily Agenda."
        sendNotification(
            context = context,
            title = title,
            message = message,
            notificationId = 8801,
            channelId = CHANNEL_TASKS
        )
    }


    fun scheduleSlotAlarm(context: Context, slot: com.aswinas.caitlin.planner.data.model.TimetableSlot): Boolean {
        return scheduleTimetableAlert(
            context = context,
            slotId = slot.id,
            timeLabel = slot.timeLabel,
            eventTitle = slot.taskTitle ?: "Focus Block",
            category = slot.category ?: "Focus"
        )
    }

    fun cancelSlotAlarm(context: Context, slotId: String) {
        cancelTimetableAlert(context, slotId)
    }

    private fun parseTriggerTimeMillis(timeLabel: String): Long? {
        val startTimePart = timeLabel.substringBefore("-").trim()
        if (startTimePart.isBlank()) return null

        val normalized = startTimePart.lowercase(Locale.getDefault())
        val wholeLabel = timeLabel.lowercase(Locale.getDefault())
        val isPm = normalized.contains("pm") || (!normalized.contains("am") && wholeLabel.contains("pm"))
        val isAm = normalized.contains("am") || (!normalized.contains("pm") && wholeLabel.contains("am"))
        val digitsOnly = normalized.replace("am", "").replace("pm", "").trim()
        val parts = digitsOnly.split(":")
        if (parts.isEmpty()) return null

        val parsedHour = parts[0].trim().toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
        if (minute !in 0..59) return null

        val hour = when {
            isPm && parsedHour in 1..11 -> parsedHour + 12
            isAm && parsedHour == 12 -> 0
            parsedHour in 0..23 -> parsedHour
            else -> return null
        }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
