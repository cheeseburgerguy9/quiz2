package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CalendarHelper {

    fun addTaskToCalendar(context: Context, task: TaskEntity): Boolean {
        return try {
            val beginTime = parseTimeToMillis(task.scheduledTime, task.createdDate)
            val endTime = beginTime + (task.estimatedMinutes * 60 * 1000L).coerceAtLeast(30 * 60 * 1000L)

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, task.title)
                putExtra(CalendarContract.Events.DESCRIPTION, buildString {
                    append(task.description)
                    append("\n\nCategory: ${task.category}")
                    append("\nPriority: ${task.priority}")
                    append("\nManaged by PlanCraft AI")
                })
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open calendar: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun addTimetableSlotToCalendar(context: Context, slot: TimetableSlot): Boolean {
        return try {
            val beginTime = parseTimeToMillis(slot.startTime, slot.date)
            val endTime = parseTimeToMillis(slot.endTime, slot.date)

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "${slot.type}: ${slot.title}")
                putExtra(CalendarContract.Events.DESCRIPTION, buildString {
                    if (slot.notes.isNotEmpty()) append(slot.notes).append("\n\n")
                    append("Scheduled by PlanCraft AI Timetable")
                })
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    if (endTime > beginTime) endTime else beginTime + 3600_000L
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open calendar: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun parseTimeToMillis(timeStr: String?, dateStr: String?): Long {
        val calendar = Calendar.getInstance()
        if (!dateStr.isNullOrEmpty()) {
            try {
                val dateParts = dateStr.split("-")
                if (dateParts.size == 3) {
                    calendar.set(Calendar.YEAR, dateParts[0].toInt())
                    calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                }
            } catch (_: Exception) {}
        }

        if (!timeStr.isNullOrBlank()) {
            try {
                // Try format like "09:30 AM" or "9:30 AM"
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                val parsed = sdf.parse(timeStr.trim())
                if (parsed != null) {
                    val parsedCal = Calendar.getInstance().apply { time = parsed }
                    calendar.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, 0)
                    return calendar.timeInMillis
                }
            } catch (_: Exception) {}

            try {
                // Try 24h format like "14:30"
                val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
                val parsed24 = sdf24.parse(timeStr.trim())
                if (parsed24 != null) {
                    val parsedCal = Calendar.getInstance().apply { time = parsed24 }
                    calendar.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, 0)
                    return calendar.timeInMillis
                }
            } catch (_: Exception) {}
        }

        // Default to now + 15 minutes
        return System.currentTimeMillis() + 15 * 60 * 1000L
    }
}
