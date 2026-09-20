package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import java.util.Calendar
import java.util.TimeZone

object CalendarHelper {
    /**
     * Inserts an event directly into the user's primary device calendar.
     * Returns true if successful, false otherwise.
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        startMillis: Long,
        durationMinutes: Int = 60,
        endMillisOverride: Long? = null
    ): Boolean {
        return try {
            val endMillis = endMillisOverride ?: (startMillis + (durationMinutes * 60 * 1000L))
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, 1) // default primary calendar ID
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri != null
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: launch calendar insert intent if direct provider insertion fails
            try {
                val endMillis = endMillisOverride ?: (startMillis + (durationMinutes * 60 * 1000L))
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, title)
                    putExtra(CalendarContract.Events.DESCRIPTION, description)
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }
}
