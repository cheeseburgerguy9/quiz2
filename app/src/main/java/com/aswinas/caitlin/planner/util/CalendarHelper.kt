package com.aswinas.caitlin.planner.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.TimeZone

object CalendarHelper {

    private fun hasCalendarPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Inserts an event into the first visible writable calendar on the device.
     * No calendar ID is hard-coded, so this works across devices and accounts.
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        startMillis: Long,
        durationMinutes: Int = 60,
        endMillisOverride: Long? = null
    ): Boolean {
        if (!hasCalendarPermission(context)) return false

        val endMillis = endMillisOverride ?: (startMillis + durationMinutes * 60_000L)
        val calendarId = findWritableCalendarId(context) ?: return false

        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values) != null
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun insertTaskEvent(
        context: Context,
        task: com.aswinas.caitlin.planner.data.model.TaskEntity
    ): Boolean = addEventToCalendar(
        context = context,
        title = task.title,
        description = "${task.category} • ${task.priority} Priority\n${task.description}",
        startMillis = System.currentTimeMillis() + 60 * 60 * 1000L
    )

    private fun findWritableCalendarId(context: Context): Long? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) !=
            PackageManager.PERMISSION_GRANTED
        ) return null

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.VISIBLE
        )

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars._ID} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val accessIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
            while (cursor.moveToNext()) {
                val access = cursor.getInt(accessIndex)
                if (access >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    return cursor.getLong(idIndex)
                }
            }
        }
        return null
    }
}
