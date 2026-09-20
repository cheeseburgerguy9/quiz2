package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: String, // e.g. "07:00 AM"
    val endTime: String,   // e.g. "08:00 AM"
    val title: String,
    val type: String,      // SLEEP, FOOD, WORK, STUDY, EXERCISE, BREAK, CHILL
    val notes: String = "",
    val date: String,      // YYYY-MM-DD
    val taskId: Long? = null,
    val calendarSynced: Boolean = false
)
